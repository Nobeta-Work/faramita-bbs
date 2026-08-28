package cn.nobeta.bbs.module.blog.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.event.DomainEvent;
import cn.nobeta.bbs.common.event.EventTypes;
import cn.nobeta.bbs.common.exception.BusinessException;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.common.util.SnowflakeUtil;
import cn.nobeta.bbs.module.blog.dto.CommentPageQuery;
import cn.nobeta.bbs.module.blog.dto.CommentSaveDTO;
import cn.nobeta.bbs.module.blog.entity.Blog;
import cn.nobeta.bbs.module.blog.entity.Comment;
import cn.nobeta.bbs.module.blog.mapper.BlogMapper;
import cn.nobeta.bbs.module.blog.mapper.CommentMapper;
import cn.nobeta.bbs.module.blog.service.CommentService;
import cn.nobeta.bbs.module.blog.vo.CommentVO;
import cn.nobeta.bbs.module.box.OutboxDomainEventPublisher;
import cn.nobeta.bbs.module.user.mapper.UserMapper;
import cn.nobeta.bbs.module.user.vo.UserBriefVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DELETED = -1;

    private final CommentMapper commentMapper;
    private final BlogMapper blogMapper;
    private final UserMapper userMapper;
    private final OutboxDomainEventPublisher eventPublisher;

    /**
     * 评论列表
     */
    @Override
    public PageResult<CommentVO> queryCommentPage(Long blogId, CommentPageQuery query) {
        requirePublishedBlog(blogId);
        // 1. 分页查询
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        Page<Comment> rootPage = commentMapper.selectRootCommentPage(blogId, query.getSortOrder());
        if (rootPage.isEmpty()) {
            return PageResult.empty(query.getPageNum(), query.getPageSize());
        }
        // 2. 获取楼层上下文
        List<Long> rootIds = rootPage.stream().map(Comment::getId).toList();
        List<Comment> replies = commentMapper.selectRepliesByRootIds(blogId, rootIds);

        List<Comment> allComments = new ArrayList<>(rootPage);
        allComments.addAll(replies);

        Set<Long> userIds = allComments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserBriefVO> userMap = userMapper.selectAuthorBriefByIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserBriefVO::getId, Function.identity()));
        Map<Long, Comment> commentMap = allComments.stream()
                .collect(Collectors.toMap(Comment::getId, Function.identity()));
        Map<Long, List<Comment>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(Comment::getRootId));

        List<CommentVO> records = rootPage.stream()
                .map(root -> {
                    List<CommentVO> replyVOs = replyMap
                            .getOrDefault(root.getId(), Collections.emptyList())
                            .stream()
                            .map(reply -> toVO(reply, userMap, commentMap, Collections.emptyList()))
                            .toList();
                    return toVO(root, userMap, commentMap, replyVOs);
                })
                .toList();

        return PageResult.<CommentVO>builder()
                .total(rootPage.getTotal())
                .pageNum(rootPage.getPageNum())
                .pageSize(rootPage.getPageSize())
                .pages(rootPage.getPages())
                .records(records)
                .build();
    }

    /**
     * 创建评论
     */
    @Override
    @Transactional
    public Long addComment(Long userId, CommentSaveDTO dto) {
        requirePublishedBlog(dto.getBlogId());

        Long commentId = SnowflakeUtil.nextId();
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        Long rootId = commentId;

        if (parentId > 0) {
            Comment parent = commentMapper.selectCommentById(parentId);
            if (parent == null || !parent.getBlogId().equals(dto.getBlogId())
                    || !Integer.valueOf(STATUS_NORMAL).equals(parent.getStatus())) {
                throw new BusinessException(ResultCode.COMMENT_PARENT_INVALID);
            }
            rootId = parent.getParentId() == 0 ? parent.getId() : parent.getRootId();
        }

        Comment comment = Comment.builder()
                .id(commentId)
                .blogId(dto.getBlogId())
                .userId(userId)
                .parentId(parentId)
                .rootId(rootId)
                .content(dto.getContent().trim())
                .likeCount(0)
                .status(STATUS_NORMAL)
                .build();

        commentMapper.insertComment(comment);
        blogMapper.incrementCommentsCount(dto.getBlogId());
        publishBlogUpdated(dto.getBlogId(), "comment-created");
        return commentId;
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectCommentById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        int affected = commentMapper.softDeleteCommentByIdAndUserId(commentId, userId);
        if (affected > 0) {
            blogMapper.decrementCommentsCount(comment.getBlogId());
            publishBlogUpdated(comment.getBlogId(), "comment-deleted");
        }
    }

    private void publishBlogUpdated(Long blogId, String reason) {
        eventPublisher.publish(
            DomainEvent.builder()
                .eventId(SnowflakeUtil.nextId())
                .eventType(EventTypes.BLOG_UPDATED)
                .aggregateType("blog")
                .aggregateId(blogId)
                .createTime(java.time.LocalDateTime.now())
                .payload(Map.of("reason", reason))
                .build()
        );
    }

    private Blog requirePublishedBlog(Long blogId) {
        Blog blog = blogMapper.selectBlogById(blogId);
        if (blog == null || !Integer.valueOf(1).equals(blog.getIsPublished())) {
            throw new BusinessException(ResultCode.BLOG_NOT_FOUND);
        }
        return blog;
    }

    private CommentVO toVO(
            Comment comment,
            Map<Long, UserBriefVO> userMap,
            Map<Long, Comment> commentMap,
            List<CommentVO> replies) {
        UserBriefVO replyTo = null;
        if (comment.getParentId() > 0) {
            Comment parent = commentMap.get(comment.getParentId());
            if (parent != null) {
                replyTo = userMap.get(parent.getUserId());
            }
        }

        boolean deleted = Integer.valueOf(STATUS_DELETED).equals(comment.getStatus());
        return CommentVO.builder()
                .id(comment.getId())
                .blogId(comment.getBlogId())
                .parentId(comment.getParentId())
                .rootId(comment.getRootId())
                .content(deleted ? null : comment.getContent())
                .likeCount(comment.getLikeCount())
                .status(comment.getStatus())
                .author(userMap.get(comment.getUserId()))
                .replyTo(replyTo)
                .replyCount(replies.size())
                .replies(replies)
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .build();
    }
}

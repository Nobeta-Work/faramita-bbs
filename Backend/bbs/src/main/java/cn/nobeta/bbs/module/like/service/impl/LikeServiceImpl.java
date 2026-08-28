package cn.nobeta.bbs.module.like.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.enums.RedisKeys;
import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.event.DomainEvent;
import cn.nobeta.bbs.common.event.EventTypes;
import cn.nobeta.bbs.common.exception.BusinessException;
import cn.nobeta.bbs.common.util.SnowflakeUtil;
import cn.nobeta.bbs.config.RedisScriptConfig;
import cn.nobeta.bbs.module.blog.entity.Blog;
import cn.nobeta.bbs.module.blog.entity.Comment;
import cn.nobeta.bbs.module.blog.mapper.BlogMapper;
import cn.nobeta.bbs.module.blog.mapper.CommentMapper;
import cn.nobeta.bbs.module.box.OutboxDomainEventPublisher;
import cn.nobeta.bbs.module.box.entity.InboxEvent;
import cn.nobeta.bbs.module.box.mapper.InboxMapper;
import cn.nobeta.bbs.module.like.dto.LikeChangedPayload;
import cn.nobeta.bbs.module.like.mapper.LikeMapper;
import cn.nobeta.bbs.module.like.service.LikeService;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private static final String CONSUMER_GROUP = "like-persist";
    private static final String BLOG_LIKE_AGGREGATE = "blog-like";
    private static final String COMMENT_LIKE_AGGREGATE = "comment-like";

    private final LikeMapper likeMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final BlogMapper blogMapper;
    private final CommentMapper commentMapper;
    private final InboxMapper inboxMapper;
    private final OutboxDomainEventPublisher eventPublisher;
    private final RedisScriptConfig redisScriptConfig;
    private final ObjectMapper objectMapper;

    /**
     * 点赞博客 (toggle 设计)
     * @param loginUser
     * @param id
     * @return
     */
    public Integer toggleBlogLike(Long userId, Long blogId) {
        // 0 校验博客是否存在
        Blog blog = blogMapper.selectBlogById(blogId);
        if (blog == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        return toggleLike(
            userId,
            blogId,
            RedisKeys.LIKE_BLOG,
            EventTypes.BLOG_LIKE_CHANGED,
            BLOG_LIKE_AGGREGATE,
            () -> likeMapper.selectLikerIdsByBlogId(blogId)
        );
    }

    /**
     * 点赞评论
     */
    @Override
    public Integer toggleCommentLike(Long userId, Long commentId) {
        Comment comment = commentMapper.selectCommentById(commentId);
        if (comment == null || !Integer.valueOf(1).equals(comment.getStatus())) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }

        return toggleLike(
            userId,
            commentId,
            RedisKeys.LIKE_COMMENT,
            EventTypes.COMMENT_LIKE_CHANGED,
            COMMENT_LIKE_AGGREGATE,
            () -> likeMapper.selectLikerIdsByCommentId(commentId)
        );
    }

    private Integer toggleLike(
        Long userId,
        Long aggregateId,
        RedisKeys likeKey,
        String eventType,
        String aggregateType,
        Supplier<List<Long>> likerIdsSupplier
    ) {
        String key = likeKey.getFullKey(aggregateId);
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            List<Long> userIds = likerIdsSupplier.get();
            String[] members = userIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new);
            if (members.length > 0) {
                stringRedisTemplate.opsForSet().add(key, members);
                stringRedisTemplate.expire(
                    key,
                    Duration.ofSeconds(likeKey.getDefaultTtl())
                );
            }
        }

        long eventId = SnowflakeUtil.nextId();
        LocalDateTime createTime = LocalDateTime.now();
        List<String> keys = List.of(
            key,
            RedisKeys.LIKE_OUTBOX_PENDING.getPrefix(),
            RedisKeys.LIKE_OUTBOX_EVENT.getFullKey(eventId)
        );
        Long count = stringRedisTemplate.execute(
            redisScriptConfig.likeToggleScript(),
            keys,
            userId.toString(),
            aggregateId.toString(),
            createTime.toString(),
            likeKey.getDefaultTtl().toString(),
            Long.toString(eventId),
            eventType,
            aggregateType,
            Long.toString(System.currentTimeMillis())
        );

        if (count == null) {
            throw new IllegalStateException("点赞脚本执行失败");
        }
        return count.intValue();
    }

    @Override
    @Transactional
    public void consumeLikeEvent(DomainEvent event) {
        if (event == null || event.getEventId() == null
            || event.getAggregateType() == null
            || event.getAggregateId() == null) {
            throw new IllegalArgumentException("点赞事件内容不完整");
        }
        // 1. 同一聚合只接受更大的事件 ID，兼顾幂等与乱序保护
        int inserted = inboxMapper.insertIfLatest(
            InboxEvent.builder()
                .consumerGroup(CONSUMER_GROUP
                    + ":" + event.getAggregateType()
                    + ":" + event.getAggregateId())
                .eventId(event.getEventId())
                .consumedTime(LocalDateTime.now())
                .build()
        );
        if (inserted == 0) {
            return;
        }

        LikeChangedPayload payload = objectMapper.convertValue(
            event.getPayload(),
            LikeChangedPayload.class
        );
        if (payload.getUserId() == null
            || payload.getLiked() == null) {
            throw new IllegalArgumentException("点赞事件内容不完整");
        }

        LocalDateTime createTime = event.getCreateTime() == null
            ? LocalDateTime.now()
            : event.getCreateTime();

        switch (event.getAggregateType()) {
            case BLOG_LIKE_AGGREGATE -> consumeBlogLike(
                event,
                payload,
                createTime
            );
            case COMMENT_LIKE_AGGREGATE -> consumeCommentLike(
                event.getAggregateId(),
                payload,
                createTime
            );
            default -> throw new IllegalArgumentException(
                "不支持的点赞聚合类型: " + event.getAggregateType()
            );
        }
    }

    private void consumeBlogLike(
        DomainEvent event,
        LikeChangedPayload payload,
        LocalDateTime createTime
    ) {
        Long blogId = event.getAggregateId();
        if (payload.getLiked()) {
            likeMapper.insertBlogLike(blogId, payload.getUserId(), createTime);
        } else {
            likeMapper.deleteBlogLike(blogId, payload.getUserId());
        }
        likeMapper.refreshBlogLikeCount(blogId);

        eventPublisher.publish(
            DomainEvent.builder()
                .eventId(SnowflakeUtil.nextId())
                .eventType(EventTypes.BLOG_UPDATED)
                .aggregateType("blog")
                .aggregateId(blogId)
                .createTime(LocalDateTime.now())
                .payload(Map.of(
                    "sourceEventId", event.getEventId(),
                    "reason", "like-count-changed"
                ))
                .build()
        );
    }

    private void consumeCommentLike(
        Long commentId,
        LikeChangedPayload payload,
        LocalDateTime createTime
    ) {
        if (payload.getLiked()) {
            likeMapper.insertCommentLike(
                commentId,
                payload.getUserId(),
                createTime
            );
        } else {
            likeMapper.deleteCommentLike(commentId, payload.getUserId());
        }
        likeMapper.refreshCommentLikeCount(commentId);
    }
}

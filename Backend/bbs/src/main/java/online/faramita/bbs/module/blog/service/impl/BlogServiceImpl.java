package online.faramita.bbs.module.blog.service.impl;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.common.enums.RedisKeys;
import online.faramita.bbs.common.enums.ResultCode;
import online.faramita.bbs.common.exception.BusinessException;
import online.faramita.bbs.common.result.PageResult;
import online.faramita.bbs.common.util.SnowflakeUtil;
import online.faramita.bbs.module.auth.dto.UserAuthInfo;
import online.faramita.bbs.module.blog.dto.BlogEditDTO;
import online.faramita.bbs.module.blog.dto.BlogPageQuery;
import online.faramita.bbs.module.blog.dto.BlogSaveDTO;
import online.faramita.bbs.module.blog.dto.BlogTagBriefRelations;
import online.faramita.bbs.module.blog.entity.Blog;
import online.faramita.bbs.module.blog.mapper.BlogMapper;
import online.faramita.bbs.module.blog.service.BlogService;
import online.faramita.bbs.module.blog.vo.BlogPrivateDetailVO;
import online.faramita.bbs.module.blog.vo.BlogPublicBriefVO;
import online.faramita.bbs.module.blog.vo.BlogPublicDetailVO;
import online.faramita.bbs.module.folder.entity.Folder;
import online.faramita.bbs.module.folder.mapper.FolderMapper;
import online.faramita.bbs.module.like.mapper.LikeMapper;
import online.faramita.bbs.module.tag.mapper.TagMapper;
import online.faramita.bbs.module.tag.vo.TagBriefVO;
import online.faramita.bbs.module.user.mapper.UserMapper;
import online.faramita.bbs.module.user.vo.UserBriefVO;
import online.faramita.bbs.security.util.SecurityUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService{
    
    private final BlogMapper blogMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final FolderMapper folderMapper;
    private final LikeMapper likeMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult<BlogPublicBriefVO> queryPublicBlogPage(BlogPageQuery query) {
        // 1.提取分页参数
        Integer pageNum = query.getPageNum();
        Integer pageSize = query.getPageSize();
        
        
        // 2. 处理 keyword
        if (query.getKeyword() != null) {
            // 去除前后空格
            query.setKeyword(query.getKeyword().trim());
        }

        
        // 2.开启PageHelper分页查询
        PageHelper.startPage(pageNum, pageSize);
        
        // 2.1 分页查询 blog
        Page<Blog> blogPage = blogMapper.selectBlogPage(query);
        if (blogPage.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 2.2 收集关联 id: blogIds:blog_tag,authorIds:sys_user
        List<Long> blogIds = blogPage.stream().map(Blog::getId).toList();
        Set<Long> authorIds = blogPage.stream().map(Blog::getAuthorId).collect(Collectors.toSet());

        // 2.3 批量查询
        Map<Long, UserBriefVO> authorMap = userMapper.selectAuthorBriefByIds(authorIds)
                .stream().collect(Collectors.toMap(UserBriefVO::getId, Function.identity()));
        Map<Long, List<TagBriefVO>> tagMap = tagMapper.selectBlogTagBriefRelationsByBlogIds(blogIds)
                .stream().collect(Collectors.groupingBy(
                    BlogTagBriefRelations::getBlogId,
                    Collectors.mapping(
                        r -> TagBriefVO.builder()
                                .id(r.getTagId())
                                .name(r.getTagName())
                                .build(),
                        Collectors.toList()
                    )
                ));

        // 2.4 组装 records
        List<BlogPublicBriefVO> records = (List<BlogPublicBriefVO>) blogPage.stream().map(
            blog -> BlogPublicBriefVO.builder()
                    .id(blog.getId())
                    .title(blog.getTitle())
                    .summary(blog.getSummary())
                    .isPublished(blog.getIsPublished())
                    .likeCount(blog.getLikeCount())
                    .createTime(blog.getCreateTime())
                    .updateTime(blog.getUpdateTime())
                    .author(authorMap.get(blog.getAuthorId()))
                    .tags(tagMap.getOrDefault(blog.getId(), Collections.emptyList()))
                    .build()
        ).toList();

        // 3. 返回
        return PageResult.<BlogPublicBriefVO>builder()
                .total(blogPage.getTotal())
                .pageNum(blogPage.getPageNum())
                .pageSize(blogPage.getPageSize())
                .pages(blogPage.getPages())
                .records(records)
                .build();
    }

    /**
     * 创建博客
     * @param userId
     * @param blogSaveDTO
     * @return
     */
    @Override
    public Long addBlogByUserId(Long userId, BlogSaveDTO blogSaveDTO) {

        // 1. 生成雪花 ID
        Long blogId = SnowflakeUtil.nextId();

        // 2. 校验目录
        Long folderId = blogSaveDTO.getFolderId() == null ? 0L : blogSaveDTO.getFolderId();
        if (folderId > 0) {
            Folder folder = folderMapper.selectFolderById(folderId);
            if (folder == null || !folder.getAuthorId().equals(userId)) {
                throw new BusinessException(ResultCode.FOLDER_NOT_FOUND);
            }
        }
        
        // 3. 封装 blog
        Blog blog = Blog.builder()
                .id(blogId)
                .authorId(userId)
                .folderId(folderId)
                .isPublished(0)
                .title(blogSaveDTO.getTitle())
                .build();

        // 4. 插入数据库

        try {
            blogMapper.insertBlog(blog);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.BLOG_TITLE_DUPLICATE);
        }

        return blogId;
    }

    /**
     * 根据 id 查询公开博客
     * @param id
     * @return
     */
    @Override
    public BlogPublicDetailVO getPublicBlogById(Long id) {

        // 1. 获得博客
        Blog blog = blogMapper.selectBlogById(id);
        if (blog == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "博客不存在");
        }

        // 2. 校验博客发布状态
        if (blog.getIsPublished() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 查找作者信息
        UserBriefVO author = userMapper.selectAuthorBriefById(blog.getAuthorId());

        // 4. 查找标签信息
        List<TagBriefVO> tags = tagMapper.selectTagBriefByBlogId(id);

        // 5. 查找 isLiked
        boolean isLiked = false;
        UserAuthInfo loginUser = SecurityUtil.getLoginUser();
        if (loginUser != null) {
            Long userId = loginUser.getUser().getId();
            String key = RedisKeys.LIKE_BLOG.getFullKey(id);
            if (!redisTemplate.hasKey(key)) {
                // TODO: 未锁机制
                // 5.1 缓存未命中，DB 回源
                List<Long> likerIds = likeMapper.selectLikerIdsByBlogId(id);
                
                // TODO: 缓存穿透

                String[] members = likerIds.stream().map(Object::toString).toArray(String[]::new);
                if (members.length > 0) {
                    redisTemplate.opsForSet().add(key, (Object[]) members);
                redisTemplate.expire(key, Duration.ofSeconds(RedisKeys.LIKE_BLOG.getDefaultTtl()));
                }
                
            }
            // 5.2 缓存查找
            isLiked = redisTemplate.opsForSet().isMember(
                key,
                userId.toString()
            );
        }

        return BlogPublicDetailVO.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .summary(blog.getSummary())
                .isPublished(blog.getIsPublished())
                .author(author)
                .tags(tags)
                .likeCount(blog.getLikeCount())
                .createTime(blog.getCreateTime())
                .updateTime(blog.getUpdateTime())
                .content(blog.getContent())
                .isLiked(isLiked)
                .build();
                
    }

    /**
     * 查询指定私人博客详情
     * @param id 博客id
     */
    @Override
    public BlogPrivateDetailVO getPrivateBlogById(Long id) {
        // 1. 获得博客
        Blog blog = blogMapper.selectBlogById(id);
        if (blog == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "博客不存在");
        }

        // 2. 校验作者信息
        UserAuthInfo loginUser = SecurityUtil.getLoginUser();
        if (loginUser == null || !loginUser.getUser().getId().equals(blog.getAuthorId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 查找作者信息
        UserBriefVO author = userMapper.selectAuthorBriefById(blog.getAuthorId());

        // 4. 查找标签信息
        List<TagBriefVO> tags = tagMapper.selectTagBriefByBlogId(id);

        

        return BlogPrivateDetailVO.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .summary(blog.getSummary())
                .isPublished(blog.getIsPublished())
                .author(author)
                .tags(tags)
                .likeCount(blog.getLikeCount())
                .createTime(blog.getCreateTime())
                .updateTime(blog.getUpdateTime())
                .folderId(blog.getFolderId())
                .content(blog.getContent())
                .build();
    }

    /**
     * 根据bloguid删除博客
     * @param blogId
     */
    @Override
    @Transactional
    public void deleteBlogById(Long blogId) {
        // 1. 查询 blog
        Blog blog = blogMapper.selectBlogById(blogId);
        if (blog == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        
        // 2. 校验权限
        UserAuthInfo loginUser = SecurityUtil.getLoginUser();
        if (loginUser == null || !loginUser.getUser().getId().equals(blog.getAuthorId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 删除数据
        blogMapper.deleteBlogById(blogId);

        // 4. 删除 blog_tag 关系
        tagMapper.deleteBlogTagRelationsByBlogId(blogId);
    }

    /**
     * 更新博客
     * @param blogId
     * @param blogEditDTO
     */
    @Transactional
    @Override
    public void editBlogById(Long blogId, BlogEditDTO blogEditDTO) {
        // 1.根据blogId查询原始博客信息
        Blog blog = blogMapper.selectBlogById(blogId);
        if (blog == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "博客不存在");
        }

        // 2.权限校验
        // TODO: 仅作者校验，无管理员权限
        UserAuthInfo loginUser = (UserAuthInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (loginUser == null || !loginUser.getUser().getId().equals(blog.getAuthorId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3.目录校验
        if (blog.getFolderId() > 0) {
            Folder folder = folderMapper.selectFolderById(blogEditDTO.getFolderId());
            if (folder == null || !folder.getAuthorId().equals(loginUser.getUser().getId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }

        // 4. blog 更新
        blogMapper.updateBlogById(blogId, blogEditDTO);

        // 5. blog_tag 双步更新
        List<Long> tagIds = blogEditDTO.getTagIds();

        tagMapper.deleteBlogTagRelationsByBlogId(blogId);

        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        
        tagMapper.batchInsertBlogTagReliations(blogId, tagIds);
    }



}

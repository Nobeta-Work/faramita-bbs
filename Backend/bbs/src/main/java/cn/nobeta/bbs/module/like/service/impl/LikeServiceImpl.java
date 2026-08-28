package cn.nobeta.bbs.module.like.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.enums.RedisKeys;
import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.exception.BusinessException;
import cn.nobeta.bbs.common.util.SnowflakeUtil;
import cn.nobeta.bbs.config.RabbitTopologyConfig;
import cn.nobeta.bbs.config.RedisScriptConfig;
import cn.nobeta.bbs.module.blog.entity.Blog;
import cn.nobeta.bbs.module.blog.mapper.BlogMapper;
import cn.nobeta.bbs.module.like.entity.LikeBlogChangelog;
import cn.nobeta.bbs.module.like.mapper.LikeMapper;
import cn.nobeta.bbs.module.like.service.LikeService;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final BlogMapper blogMapper;
    private final RedisScriptConfig redisScriptConfig;

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
        // 1. 查看缓存
        String key = RedisKeys.LIKE_BLOG.getFullKey(blogId);
        if (!stringRedisTemplate.hasKey(key)) {
            // 1.1 缓存不存在，读取  DB 回写
            // TODO: 缓存穿透
            Duration ttl = Duration.ofSeconds(RedisKeys.LIKE_BLOG.getDefaultTtl());

            List<Long> userIds = likeMapper.selectLikerIdsByBlogId(blogId);
            String[] members = userIds.stream()
                    .map(String::valueOf).toArray(String[]::new);
            if (members.length > 0) {
                stringRedisTemplate.opsForSet().add(
                    key, 
                    members
                );
                stringRedisTemplate.expire(key, ttl);
            }
        }

        // 2. 缓存存在，读取用户是否点赞
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
            blogId.toString(),
            createTime.toString(),
            RedisKeys.LIKE_BLOG.getDefaultTtl().toString(),
            Long.toString(eventId),
            RabbitTopologyConfig.BLOG_LIKE_CHANGED_ROUTING_KEY,
            "blog-like",
            Long.toString(System.currentTimeMillis())
        );

        // 3. 返回当前点赞数量
        return count.intValue();
    }


    /**
     * 定时消费 like:blog:changelog 任务队列
     * 异步回写数据库
     */
    @Override
    @Transactional
    public void flushLikeBlogChangelog(List<LikeBlogChangelog> logs) {
        
        // 除旧迎新 Map<blogId:userId, log>
        Map<String, LikeBlogChangelog> latestMap = new LinkedHashMap<>();
        for (LikeBlogChangelog log : logs) {
            String mapKey = log.getBlogId() + ":" + log.getUserId();
            latestMap.put(mapKey, log);
        }

        // 新增 like 队列 -> INSERT
        List<LikeBlogChangelog> likeList = latestMap.values().stream()
                .filter(LikeBlogChangelog::isLikeAction).toList();

        // 去除 like 队列 -> DELETE
        List<LikeBlogChangelog> unlikeList = latestMap.values().stream()
                .filter(log -> !log.isLikeAction()).toList();
        
        // 获取所有影响 blogId -> UPDATE / REFRESH
        List<Long> blogIds = latestMap.values().stream()
                .map(LikeBlogChangelog::getBlogId)
                .distinct()
                .toList();

        // 批量 INSERT
        if (!likeList.isEmpty()) {
            likeMapper.batchInsertBlogLikeByChanges(likeList);
        }
        // 批量 DELETE
        if (!unlikeList.isEmpty()) {
            likeMapper.batchDeleteBlogLikeByChanges(unlikeList);
        }
        // 批量 UPDATE
        if (!blogIds.isEmpty()) {
            likeMapper.refreshBlogLikeCountByBlogIds(blogIds);
        }

    }

}

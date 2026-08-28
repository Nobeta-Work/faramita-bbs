package cn.nobeta.bbs.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.nobeta.bbs.module.blog.mapper.BlogMapper;
import cn.nobeta.bbs.module.blog.service.BlogSearchIndexer;
import cn.nobeta.bbs.module.like.mapper.LikeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConsistencyReconciliationTask {

    private final BlogMapper blogMapper;
    private final LikeMapper likeMapper;
    private final BlogSearchIndexer blogSearchIndexer;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void reconcile() {
        likeMapper.reconcileBlogLikeCount();
        likeMapper.reconcileCommentLikeCount();
        blogMapper.reconcileCommentsCount();

        for (Long blogId : blogMapper.selectPublishedBlogIds()) {
            try {
                blogSearchIndexer.syncBlog(blogId);
            } catch (RuntimeException e) {
                log.warn("Failed to reconcile blog search document, blogId={}", blogId, e);
            }
        }
    }
}

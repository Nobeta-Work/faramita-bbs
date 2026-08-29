package cn.nobeta.bbs.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import cn.nobeta.bbs.module.blog.entity.BlogSearchDocument;
import cn.nobeta.bbs.module.blog.mapper.BlogMapper;
import cn.nobeta.bbs.module.blog.service.BlogSearchIndexer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer
    implements ApplicationRunner {

    private final ElasticsearchOperations operations;
    private final BlogMapper blogMapper;
    private final BlogSearchIndexer blogSearchIndexer;

    @Override
    public void run(ApplicationArguments args) {
        IndexOperations index = 
            operations.indexOps(BlogSearchDocument.class);

        if (index.exists() || !index.createWithMapping()) {
            return;
        }

        for (Long blogId : blogMapper.selectPublishedBlogIds()) {
            try {
                blogSearchIndexer.syncBlog(blogId);
            } catch (RuntimeException e) {
                log.warn(
                    "Failed to initialize blog search document, blogId={}",
                    blogId,
                    e
                );
            }
        }
    }
}

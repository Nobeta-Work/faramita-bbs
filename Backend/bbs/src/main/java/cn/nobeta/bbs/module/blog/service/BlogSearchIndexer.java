package cn.nobeta.bbs.module.blog.service;

import java.time.LocalDateTime;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.nobeta.bbs.common.event.DomainEvent;
import cn.nobeta.bbs.module.blog.entity.BlogSearchDocument;
import cn.nobeta.bbs.module.blog.mapper.BlogMapper;
import cn.nobeta.bbs.module.box.entity.InboxEvent;
import cn.nobeta.bbs.module.box.mapper.InboxMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogSearchIndexer {

    private static final String CONSUMER_GROUP = "blog-search-index";

    private final BlogMapper blogMapper;
    private final ElasticsearchOperations elasticsearchOperations;
    private final InboxMapper inboxMapper;

    @Transactional
    public void consume(DomainEvent event) {
        if (event == null || event.getEventId() == null
            || event.getAggregateType() == null
            || event.getAggregateId() == null) {
            throw new IllegalArgumentException("博客搜索事件内容不完整");
        }

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

        syncBlog(event.getAggregateId());
    }

    public void syncBlog(Long blogId) {
        BlogSearchDocument document = 
            blogMapper.selectSearchDocumentById(blogId);

        if (document == null) {
            // 博客未发布 || 博客删除 -> 删除 ES 文档
            elasticsearchOperations.delete(blogId.toString(), BlogSearchDocument.class);
            return;
        }

        // 博客存在 && 公开 -> 新增/覆盖 ES 文档
        elasticsearchOperations.save(document);
    }
}

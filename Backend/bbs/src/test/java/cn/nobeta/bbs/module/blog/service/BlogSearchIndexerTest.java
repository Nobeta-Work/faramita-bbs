package cn.nobeta.bbs.module.blog.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import cn.nobeta.bbs.common.event.DomainEvent;
import cn.nobeta.bbs.module.blog.entity.BlogSearchDocument;
import cn.nobeta.bbs.module.blog.mapper.BlogMapper;
import cn.nobeta.bbs.module.box.entity.InboxEvent;
import cn.nobeta.bbs.module.box.mapper.InboxMapper;

@ExtendWith(MockitoExtension.class)
class BlogSearchIndexerTest {

    @Mock
    private BlogMapper blogMapper;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private InboxMapper inboxMapper;

    @InjectMocks
    private BlogSearchIndexer indexer;

    @Test
    void shouldSkipDuplicatedEvent() {
        DomainEvent event = event();
        when(inboxMapper.insertIgnore(any(InboxEvent.class))).thenReturn(0);

        indexer.consume(event);

        verify(blogMapper, never()).selectSearchDocumentById(any());
        verifyNoInteractions(elasticsearchOperations);
    }

    @Test
    void shouldSavePublishedBlogAfterInboxClaimed() {
        DomainEvent event = event();
        BlogSearchDocument document = BlogSearchDocument.builder()
            .id(event.getAggregateId())
            .build();
        when(inboxMapper.insertIgnore(any(InboxEvent.class))).thenReturn(1);
        when(blogMapper.selectSearchDocumentById(event.getAggregateId()))
            .thenReturn(document);

        indexer.consume(event);

        verify(elasticsearchOperations).save(document);
    }

    private DomainEvent event() {
        return DomainEvent.builder()
            .eventId(100L)
            .eventType("blog.updated")
            .aggregateType("blog")
            .aggregateId(200L)
            .createTime(LocalDateTime.now())
            .build();
    }
}

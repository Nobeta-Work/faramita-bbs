package cn.nobeta.bbs.module.blog.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cn.nobeta.bbs.common.event.DomainEvent;
import cn.nobeta.bbs.config.RabbitTopologyConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlogSearchConsumer {
    
    private final BlogSearchIndexer blogSearchIndexer;

    @RabbitListener(
        queues = RabbitTopologyConfig.BLOG_SEARCH_QUEUE,
        concurrency = "1"
    )
    public void consume(DomainEvent event) {
        blogSearchIndexer.syncBlog(event.getAggregateId());
    }
}

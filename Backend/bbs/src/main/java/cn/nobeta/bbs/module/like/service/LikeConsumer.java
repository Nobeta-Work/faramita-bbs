package cn.nobeta.bbs.module.like.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cn.nobeta.bbs.common.event.DomainEvent;
import cn.nobeta.bbs.config.RabbitTopologyConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikeConsumer {

    private final LikeService likeService;

    @RabbitListener(queues = RabbitTopologyConfig.LIKE_PERSIST_QUEUE)
    public void consume(DomainEvent event) {
        likeService.consumeLikeEvent(event);
    }
}

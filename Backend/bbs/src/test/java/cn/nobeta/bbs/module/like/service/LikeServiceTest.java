package cn.nobeta.bbs.module.like.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.nobeta.bbs.common.event.DomainEvent;
import cn.nobeta.bbs.common.event.EventTypes;
import cn.nobeta.bbs.config.RedisScriptConfig;
import cn.nobeta.bbs.module.blog.mapper.BlogMapper;
import cn.nobeta.bbs.module.blog.mapper.CommentMapper;
import cn.nobeta.bbs.module.box.OutboxDomainEventPublisher;
import cn.nobeta.bbs.module.box.entity.InboxEvent;
import cn.nobeta.bbs.module.box.mapper.InboxMapper;
import cn.nobeta.bbs.module.like.mapper.LikeMapper;
import cn.nobeta.bbs.module.like.service.impl.LikeServiceImpl;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {

    @InjectMocks
    private LikeServiceImpl likeService;

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private BlogMapper blogMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private InboxMapper inboxMapper;

    @Mock
    private OutboxDomainEventPublisher eventPublisher;

    @Mock
    private RedisScriptConfig redisScriptConfig;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void whenEventAlreadyConsumed_skipBusinessWrite() {
        DomainEvent event = event("blog-like", true);
        when(inboxMapper.insertIfLatest(any(InboxEvent.class))).thenReturn(0);

        likeService.consumeLikeEvent(event);

        verify(likeMapper, never()).insertBlogLike(any(), any(), any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void whenBlogLikeEvent_persistAndPublishBlogUpdate() {
        DomainEvent event = event("blog-like", true);
        when(inboxMapper.insertIfLatest(any(InboxEvent.class))).thenReturn(1);

        likeService.consumeLikeEvent(event);

        verify(likeMapper).insertBlogLike(10L, 20L, event.getCreateTime());
        verify(likeMapper).refreshBlogLikeCount(10L);

        ArgumentCaptor<DomainEvent> captor =
            ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertEquals(EventTypes.BLOG_UPDATED, captor.getValue().getEventType());
        assertEquals(10L, captor.getValue().getAggregateId());
    }

    @Test
    void whenCommentUnlikeEvent_deleteAndRefreshCount() {
        DomainEvent event = event("comment-like", false);
        when(inboxMapper.insertIfLatest(any(InboxEvent.class))).thenReturn(1);

        likeService.consumeLikeEvent(event);

        verify(likeMapper).deleteCommentLike(10L, 20L);
        verify(likeMapper).refreshCommentLikeCount(10L);
        verify(eventPublisher, never()).publish(any());
    }

    private DomainEvent event(String aggregateType, boolean liked) {
        return DomainEvent.builder()
            .eventId(1L)
            .eventType("like.changed")
            .aggregateType(aggregateType)
            .aggregateId(10L)
            .createTime(LocalDateTime.of(2026, 8, 29, 5, 0))
            .payload(Map.of("userId", 20L, "liked", liked))
            .build();
    }
}

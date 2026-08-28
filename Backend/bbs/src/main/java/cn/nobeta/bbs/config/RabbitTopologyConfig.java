package cn.nobeta.bbs.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitTopologyConfig {

    public static final String 
        DOMAIN_EXCHANGE = "parabbs.domain.v1";

    public static final String
        COMMAND_EXCHANGE = "parabbs.command.v1";

    public static final String
        DEAD_LETTER_EXCHANGE = "parabbs.dead-letter.v1";

    public static final String
        COMMENT_COUNT_QUEUE = "parabbs.projection.comment-count.v1";

    public static final String
        COMMENT_COUNT_DLQ = "parabbs.projection.comment-count.dlq.v1";

    public static final String
        BLOG_SEARCH_QUEUE = "parabbs.search.blog-index.v1";

    public static final String
        BLOG_SEARCH_DLQ = "parabbs.search.blog-index.dlq.v1";

    public static final String
        LIKE_PERSIST_QUEUE = "parabbs.like.persist.v1";

    public static final String
        LIKE_PERSIST_DLQ = "parabbs.like.persist.dlq.v1";

    public static final String
        LIKE_CHANGED_ROUTING_PATTERN = "*.like.changed";

    @Bean
    TopicExchange domainExchange() {
        return ExchangeBuilder
            .topicExchange(DOMAIN_EXCHANGE)
            .durable(true)
            .build();
    }

    @Bean
    TopicExchange commandExchange() {
        return ExchangeBuilder
            .topicExchange(COMMAND_EXCHANGE)
            .durable(true)
            .build();
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return ExchangeBuilder
            .directExchange(DEAD_LETTER_EXCHANGE)
            .durable(true)
            .build();
    }

    @Bean
    Queue blogSearchQueue() {
        return QueueBuilder
            .durable(BLOG_SEARCH_QUEUE)
            .deadLetterExchange(DEAD_LETTER_EXCHANGE)
            .deadLetterRoutingKey(BLOG_SEARCH_DLQ)
            .build();
    }

    @Bean
    Queue blogSearchDeadLetterQueue() {
        return QueueBuilder
            .durable(BLOG_SEARCH_DLQ)
            .build();
    }

    @Bean
    Queue likePersistQueue() {
        return QueueBuilder
            .durable(LIKE_PERSIST_QUEUE)
            .deadLetterExchange(DEAD_LETTER_EXCHANGE)
            .deadLetterRoutingKey(LIKE_PERSIST_DLQ)
            .build();
    }

    @Bean
    Queue likePersistDeadLetterQueue() {
        return QueueBuilder
            .durable(LIKE_PERSIST_DLQ)
            .build();
    }

    @Bean
    Binding blogSearchBinding(
        @Qualifier("blogSearchQueue") Queue queue,
        @Qualifier("domainExchange") TopicExchange exchange
    ) {
        return BindingBuilder.bind(queue)
            .to(exchange)
            .with("blog.#");
    }

    @Bean
    Binding blogSearchDeadLetterBinding(
        @Qualifier("blogSearchDeadLetterQueue") Queue queue,
        DirectExchange deadLetterExchange
    ) {
        return BindingBuilder.bind(queue)
            .to(deadLetterExchange)
            .with(BLOG_SEARCH_DLQ);
    }

    @Bean
    Binding likePersistBinding(
        @Qualifier("likePersistQueue") Queue queue,
        @Qualifier("commandExchange") TopicExchange exchange
    ) {
        return BindingBuilder.bind(queue)
            .to(exchange)
            .with(LIKE_CHANGED_ROUTING_PATTERN);
    }

    @Bean
    Binding likePersistDeadLetterBinding(
        @Qualifier("likePersistDeadLetterQueue") Queue queue,
        DirectExchange deadLetterExchange
    ) {
        return BindingBuilder.bind(queue)
            .to(deadLetterExchange)
            .with(LIKE_PERSIST_DLQ);
    }
}

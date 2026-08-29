/**
 * v0.5.0 database migration.
 * Adds comments, comment likes, and Outbox/Inbox event storage.
 *
 * This script is safe to run repeatedly.
 */

USE `para_bbs`;

/* ========== Blog comments ========== */

/* Add the denormalized comment count once. */
SET @add_comments_count = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'blog'
          AND COLUMN_NAME = 'comments_count'
    ),
    'SELECT 1',
    'ALTER TABLE `blog` ADD COLUMN `comments_count` INT NOT NULL DEFAULT 0 COMMENT ''评论数'' AFTER `like_count`'
);
PREPARE add_comments_count_stmt FROM @add_comments_count;
EXECUTE add_comments_count_stmt;
DEALLOCATE PREPARE add_comments_count_stmt;

CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT NOT NULL COMMENT '评论主键（应用层雪花 ID）',
    `blog_id` BIGINT NOT NULL COMMENT '所属博客主键',
    `user_id` BIGINT NOT NULL COMMENT '评论用户主键',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父评论主键，0 为顶级评论',
    `root_id` BIGINT NOT NULL COMMENT '根评论主键，顶级评论为自身 ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1:正常,0:草稿,-1:已删除,-2:隐藏',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_blog_parent_create` (`blog_id`, `parent_id`, `create_time`),
    KEY `idx_blog_root_create` (`blog_id`, `root_id`, `create_time`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='博客评论表';

CREATE TABLE IF NOT EXISTS `like_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `comment_id` BIGINT NOT NULL COMMENT '评论主键',
    `user_id` BIGINT NOT NULL COMMENT '用户主键',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='评论点赞记录表';

/* ========== Outbox and Inbox ========== */

CREATE TABLE IF NOT EXISTS `outbox_event` (
    `id` BIGINT NOT NULL COMMENT '事件主键',
    `event_type` VARCHAR(64) NOT NULL COMMENT '事件类型',
    `aggregate_type` VARCHAR(32) DEFAULT NULL COMMENT '聚合类型',
    `aggregate_id` BIGINT DEFAULT NULL COMMENT '业务实体主键',
    `payload` JSON NOT NULL COMMENT '事件数据',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0:待发送,1:发送中,2:已发送,3:失败',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `next_retry_time` DATETIME DEFAULT NULL COMMENT '下次可发布时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `published_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY `idx_outbox_pending` (`status`, `next_retry_time`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='领域事件发件箱';

CREATE TABLE IF NOT EXISTS `inbox_event` (
    `consumer_group` VARCHAR(64) NOT NULL COMMENT '消费组',
    `event_id` BIGINT NOT NULL COMMENT '事件主键',
    `consumed_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
    PRIMARY KEY (`consumer_group`, `event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='领域事件收件箱';

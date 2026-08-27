/**
 * v0.4.1 database migration.
 * Adds Agent MCP support to an existing para_bbs schema.
 *
 * This script is safe to run repeatedly.
 */

USE `para_bbs`;

/* ========== Agent MCP ========== */

/* Add the Agent role. */
INSERT IGNORE INTO `sys_role` (
    `id`, `role_code`, `role_name`, `description`
) VALUES (
    3, 'AGENT', 'AI助手', 'AI 助手角色，拥有博客的增删改查权限'
);

/* Store user-owned Agent tokens. */
CREATE TABLE IF NOT EXISTS `agent_token` (
    `token` BIGINT NOT NULL COMMENT '主键、业务键',
    `user_id` BIGINT NOT NULL COMMENT '所属用户',
    `name` VARCHAR(50) NOT NULL COMMENT '命名',
    `expire` INT NOT NULL COMMENT '有效时长，单位"天"，-1 为永久',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    PRIMARY KEY (`token`),
    UNIQUE KEY `uk_user_name` (`user_id`, `name`) COMMENT '同一用户下 Token 名唯一',
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Agent Token 表';

/* Grant the permissions required by AgentAuthenticationFilter. */
INSERT IGNORE INTO `sys_role_perm` (`role_id`, `perm_id`)
SELECT r.`id`, p.`id`
FROM `sys_role` r
JOIN `sys_perm` p
WHERE r.`role_code` = 'AGENT'
  AND p.`perm_code` IN (
      'blog:manage_self',
      'folder:manage_self'
  );

/* ========== v0.5.0 Comment and Outbox ========== */

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

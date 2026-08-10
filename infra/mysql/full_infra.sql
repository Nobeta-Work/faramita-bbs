/**
 * version: v0.4.0
 * 完整数据库sql脚本，与迁移无关
 */

CREATE DATABASE IF NOT EXISTS `para_bbs`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `para_bbs`;

CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
    `nickname` VARCHAR(50) NOT NULL COMMENT '用户昵称',
    `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像相对路径',
    `sex` TINYINT NOT NULL DEFAULT 2 COMMENT '性别 0:女,1:男,2:未设置',
    `race` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '种族',
    `signature` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '个性签名',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0:封禁,1:正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL COMMENT '角色主键',
    `role_code` VARCHAR(20) NOT NULL COMMENT '角色唯一编码',
    `role_name` VARCHAR(20) NOT NULL COMMENT '角色名',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    UNIQUE KEY `uk_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

CREATE TABLE `sys_perm` (
    `id` BIGINT NOT NULL COMMENT '权限主键',
    `perm_code` VARCHAR(64) NOT NULL COMMENT '权限唯一编码',
    `perm_name` VARCHAR(50) NOT NULL COMMENT '权限名',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '权限描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_perm_code` (`perm_code`),
    UNIQUE KEY `uk_perm_name` (`perm_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限表';

CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户主键',
    `role_id` BIGINT NOT NULL COMMENT '角色主键',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关系表';

CREATE TABLE `sys_role_perm` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id` BIGINT NOT NULL COMMENT '角色主键',
    `perm_id` BIGINT NOT NULL COMMENT '权限主键',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `perm_id`),
    KEY `idx_perm_id` (`perm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关系表';

CREATE TABLE `folder` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '目录主键',
    `author_id` BIGINT NOT NULL COMMENT '作者主键',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父目录主键，0 为根目录',
    `name` VARCHAR(50) NOT NULL COMMENT '目录名',
    `path` VARCHAR(255) NOT NULL COMMENT '目录路径',
    `level` TINYINT NOT NULL COMMENT '目录层级',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_author_parent_name` (`author_id`, `parent_id`, `name`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客目录表';

CREATE TABLE `blog` (
    `id` BIGINT NOT NULL COMMENT '博客主键（应用层雪花 ID）',
    `author_id` BIGINT NOT NULL COMMENT '作者主键',
    `folder_id` BIGINT NOT NULL DEFAULT 0 COMMENT '目录主键，0 为根目录',
    `is_published` TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开',
    `title` VARCHAR(255) NOT NULL COMMENT '标题',
    `summary` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '摘要',
    `content` MEDIUMTEXT NOT NULL COMMENT '正文',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_author_folder_title` (`author_id`, `folder_id`, `title`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_folder_id` (`folder_id`),
    KEY `idx_published_create_time` (`is_published`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客表';

CREATE TABLE `tag` (
    `id` BIGINT NOT NULL COMMENT '标签主键（应用层雪花 ID）',
    `name` VARCHAR(20) NOT NULL COMMENT '标签名',
    `description` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '标签描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

CREATE TABLE `blog_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `blog_id` BIGINT NOT NULL COMMENT '博客主键',
    `tag_id` BIGINT NOT NULL COMMENT '标签主键',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_tag` (`blog_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客标签关系表';

CREATE TABLE `like_blog` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `blog_id` BIGINT NOT NULL COMMENT '博客主键',
    `user_id` BIGINT NOT NULL COMMENT '用户主键',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_user` (`blog_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客点赞记录表';

CREATE TABLE `avatar_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `file_uuid` VARCHAR(255) NOT NULL COMMENT '存储文件唯一标识',
    `is_referenced` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已关联用户',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联用户主键',
    `expire_time` DATETIME DEFAULT NULL COMMENT '未关联文件过期时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_file_uuid` (`file_uuid`),
    KEY `idx_expire_time` (`expire_time`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='头像文件生命周期表';

INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`) VALUES
    (1, 'USER', '普通用户', '默认注册用户'),
    (2, 'ADMIN', '管理员', '系统管理员');

INSERT INTO `sys_perm` (`id`, `perm_code`, `perm_name`, `description`) VALUES
    (1, 'user:edit_profile', '编辑个人资料', '修改自己的个人资料'),
    (2, 'blog:manage_self', '管理个人博客', '创建、编辑、删除自己的博客'),
    (3, 'folder:manage_self', '管理个人目录', '创建、移动、删除自己的目录'),
    (4, 'like:blog', '博客点赞', '点赞或取消点赞博客'),
    (5, 'user:ban', '封禁用户', '管理员封禁用户'),
    (6, 'admin:manage', '系统管理', '管理员系统管理权限');

INSERT INTO `sys_role_perm` (`role_id`, `perm_id`) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4),
    (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6);

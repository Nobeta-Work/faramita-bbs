/* ========== 用户认证系统 ==========*/
/*
用户表 user -> sys_user
delete login_fail_count, lock_time, is_locked
add status
*/
RENAME TABLE `user` TO `sys_user`;

ALTER TABLE `sys_user`
DROP COLUMN login_fail_count,
DROP COLUMN lock_time,
DROP COLUMN is_locked,
ADD COLUMN status TINYINT DEFAULT 1 COMMENT '状态 0:封禁,1:正常';

/*
新建实体表 sys_role, sys_perm
新建关系表 sys_user_role, sys_role_perm
*/
CREATE TABLE `sys_role` (
    id BIGINT NOT NULL COMMENT '角色主键id(雪花)',
    role_code VARCHAR(20) NOT NULL COMMENT '角色唯一编码',
    role_name VARCHAR(20) NOT NULL COMMENT '角色名',
    description VARCHAR(255) COMMENT '角色描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`) COMMENT '角色编码唯一',
    UNIQUE KEY `uk_role_name` (`role_name`) COMMENT '角色名唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT '系统角色表';

CREATE TABLE `sys_perm` (
    id BIGINT NOT NULL COMMENT '权限主键id(雪花)',
    perm_code VARCHAR(20) NOT NULL COMMENT '权限唯一编码',
    perm_name VARCHAR(20) NOT NULL COMMENT '权限名',
    description VARCHAR(200) COMMENT '权限描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
    COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_perm_code` (`perm_code`) COMMENT '权限编码唯一',
    UNIQUE KEY `uk_perm_name` (`perm_name`) COMMENT '权限名唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT '系统权限表';

CREATE TABLE `sys_user_role` (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    user_id BIGINT NOT NULL COMMENT '用户id',
    role_id BIGINT NOT NULL COMMENT '角色id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`) COMMENT '用户不能重复持有角色',
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT '系统用户-角色关系表';

CREATE TABLE `sys_role_perm` (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    role_id BIGINT NOT NULL COMMENT '角色id',
    perm_id BIGINT NOT NULL COMMENT '权限id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `perm_id`) COMMENT '角色不能重复获得权限',
    KEY `idx_perm_id` (`perm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT '系统角色-权限关系表';

/* ========== 博客系统 ========*/
/*
修改 博客表 blog 
删除 大类表 blog_big_category, 小类表 blog_category
新建 标签表 tag, 目录表 folder
*/

-- 删除 blog_big_category, blog_category
DROP TABLE blog_big_category, blog_category;

/*
博客表 blog
drop: bloguid, category_id, little_category_name, author_name, big_category_id
add: folder_id, like_count
去除 id 的自增性，旧博客保持旧自增id，新博客id由应用层赋值雪花id
*/

ALTER TABLE blog
DROP COLUMN bloguid,
DROP COLUMN category_id,
DROP COLUMN little_category_name,
DROP COLUMN author_name,
DROP COLUMN big_category_id,
MODIFY id BIGINT NOT NULL COMMENT '主键id(雪花)',
ADD COLUMN folder_id BIGINT NOT NULL DEFAULT 0 COMMENT '目录id(雪花) 0:根目录',
ADD COLUMN like_count INT NOT NULL DEFAULT 0 COMMENT '总点赞数量',
UNIQUE KEY `uk_author_folder_title` (`author_id`, `folder_id`, `title`) COMMENT '同一用户同一目录下禁止重名博客'
KEY `idx_folder_id` (`folder_id`);

-- 创建 tag 表
CREATE TABLE `tag` (
    id BIGINT NOT NULL COMMENT '主键id(雪花)',
    name VARCHAR(20) NOT NULL COMMENT '标签名',
    description VARCHAR(200) NOT NULL DEFAULT '' COMMENT '标签描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`) COMMENT '标签名唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT '标签表';

-- 创建 folder 表
CREATE TABLE `folder` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键id(自增)',
    `author_id` BIGINT NOT NULL COMMENT '作者id',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父目录id',
    `name` VARCHAR(50) NOT NULL COMMENT '目录名',
    `path` VARCHAR(255) NOT NULL COMMENT '路径 /:根目录',
    `level` TINYINT NOT NULL COMMENT '目录等级 1:一级目录',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序等级',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_author_parent_name` (`author_id`, `parent_id`, `name`) COMMENT '同一用户同一目录下子目录禁止重名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT '目录表 level:0 path:/ 为根目录，无数据映射，应用层约束';

-- 创建 blog_tag 关系表
CREATE TABLE `blog_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `blog_id` BIGINT NOT NULL COMMENT '博客id',
    `tag_id` BIGINT NOT NULL COMMENT '标签id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_tag` (`blog_id`, `tag_id`) COMMENT '同一博客拥有的标签禁止重复',
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT '博客-标签关系表';

/* ========== 点赞系统 ========*/
CREATE TABLE `like_blog` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `blog_id` BIGINT NOT NULL COMMENT '博客id',
    `user_id` BIGINT NOT NULL COMMENT '用户id',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_blog_user` (`blog_id`, `user_id`) COMMENT '同一用户只能给一个博客点一次赞',
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT '博客点赞记录表'
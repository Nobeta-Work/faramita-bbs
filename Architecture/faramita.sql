/* # 用户表设计 */
CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'uid',
    username VARCHAR(30) NOT NULL COMMENT '登录账号',
    password VARCHAR(255) NOT NULL COMMENT '加密后密码',
    nickname VARCHAR(20) NOT NULL COMMENT '用户昵称',
    avatar VARCHAR(255) DEFAULT 'DEFAULT_AVATOR.jpg' COMMENT '头像URL',
    sex TINYINT DEFAULT '2' COMMENT '性别：0=女,1=男,2=神秘',
    race VARCHAR(20) DEFAULT '人类' COMMENT '种族',
    signature VARCHAR(200) DEFAULT '用户很神秘' COMMENT '个性签名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '个人资料更新日期',
    login_fail_count INT COMMENT '登录失败次数',
    lock_time DATETIME COMMENT '锁定时间',
    is_locked TINYINT DEFAULT '0' COMMENT '是否锁定：0=正常,1=锁定',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username) COMMENT '账号唯一索引'
) COMMENT = '用户表';

/* # 头像文件表设计 */
CREATE TABLE avatar_info (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    file_uuid VARCHAR(128) NOT NULL COMMENT '文件唯一标识，存储索引',
    is_referenced TINYINT NOT NULL DEFAULT '0' COMMENT '是否被引用：0=未关联账号,1=管理账号',
    user_id BIGINT COMMENT '关联用户id',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_uuid (file_uuid) COMMENT 'UUID+后缀唯一索引',
    KEY idx_is_referenced_expire (is_referenced, expire_time) COMMENT '引用状态+过期时间索引'
) COMMENT = '头像文件信息表';


/* # 博客大类表 */
CREATE TABLE blog_big_category (
    id INT NOT NULL AUTO_INCREMENT COMMENT '大类ID',
    name VARCHAR(50) NOT NULL COMMENT '大类名称',
    PRIMARY KEY (id),
    UNIQUE INDEX idx_name (name)
) COMMENT = '博客大类表';
--- 初始化数据
INSERT INTO blog_big_category (name) VALUES ('项目');
INSERT INTO blog_big_category (name) VALUES ('技术栈');
INSERT INTO blog_big_category (name) VALUES ('算法');
INSERT INTO blog_big_category (name) VALUES ('游戏');
INSERT INTO blog_big_category (name) VALUES ('余文');

/* # 博客分类表 */
CREATE TABLE blog_category (
    id INT NOT NULL AUTO_INCREMENT COMMENT '主键、小类id',
    name VARCHAR(50) NOT NULL COMMENT '小类名称',
    big_category_id INT NOT NULL COMMENT '所属大类ID',
    user_id BIGINT NOT NULL COMMENT '创建者id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX idx_user_big_name (user_id, big_category_id, name) -- 同一用户同一大类下小类唯一
) COMMENT = '博客分类表';

/* # 博客表 */
CREATE TABLE blog (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键、文章id',
    bloguid VARCHAR(50) NOT NULL COMMENT '唯一标识uuid',
    title VARCHAR(255) NOT NULL COMMENT '文章标题',
    content LONGTEXT NOT NULL COMMENT '文章内容',
    summary VARCHAR(500) NULL COMMENT '文章摘要',
    author_id BIGINT NOT NULL COMMENT '作者id',
    author_name VARCHAR(30) NOT NULL COMMENT '作者昵称',
    category_id INT NOT NULL COMMENT '所属分类id',
    big_category_id BIGINT NOT NULL COMMENT '所属大类id',
    little_category_name VARCHAR(30) NOT NULL COMMENT '所属小类名',
    is_published TINYINT NOT NULL DEFAULT 0 COMMENT '是否发布:0=草稿,1=发布',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE INDEX idx_bloguid (bloguid),
    INDEX idx_author_id (author_id),
    INDEX idx_category_id (category_id),
    INDEX idx_create_time (create_time)
) COMMENT = '博客文章表';

/* # 评论表 */
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `blog_id` BIGINT NOT NULL COMMENT '所属文章ID (逻辑上关联 blog.id)',
  `user_id` BIGINT NOT NULL COMMENT '评论者ID (逻辑上关联 user.id)',
  `parent_id` BIGINT NULL COMMENT '父评论ID，用于实现回复功能。NULL表示顶级评论 (逻辑上关联 comment.id)',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  INDEX `idx_blog_id` (`blog_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_parent_id` (`parent_id`) -- 为 parent_id 建立索引
) COMMENT = '评论表（支持回复功能）';
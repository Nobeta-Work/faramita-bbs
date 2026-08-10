USE para_bbs;

-- 将 user 表的 nickname 字段设为唯一键
ALTER TABLE `sys_user`
UNIQUE KEY `uk_nickname` (`nickname`)
COMMENT '用户昵称唯一';

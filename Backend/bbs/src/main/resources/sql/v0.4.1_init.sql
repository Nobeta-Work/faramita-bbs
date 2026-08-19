USE para_bbs;

/* ========== v0.4.1 Agent MCP ==========*/

/*
sys_role 增加 Agent 角色
*/
INSERT IGNORE INTO sys_role (
    id, role_code, role_name, description
) VALUES (3, 'AGENT', 'AI助手', 'AI 助手角色，拥有博客的增删改查权限');

/*
新建表 agent_token
Field: token(BIGINT,主键), user_id(BIGINT), name(VARCHAR(50)), expire(INT,单位天,-1永久)
      create_time, update_time, is_deleted
联合索引: (user_id, name)
*/
CREATE TABLE IF NOT EXISTS `agent_token` (
    token BIGINT NOT NULL COMMENT '主键、业务键',
    user_id BIGINT NOT NULL COMMENT '所属用户',
    name VARCHAR(50) NOT NULL COMMENT '命名',
    expire INT NOT NULL COMMENT '有效时长，单位"天"，-1 为永久',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    PRIMARY KEY (`token`),
    UNIQUE KEY `uk_user_name` (`user_id`, `name`) COMMENT '同一用户下 Token 名唯一',
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Agent Token 表';

/*
Agent 角色绑定权限：
- 管理个人博客（查看/阅读/编辑账号博客）
- 管理个人目录（查看账号目录列表）
*/
INSERT IGNORE INTO sys_role_perm (
    role_id, perm_id
)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_perm p
WHERE r.role_code = 'AGENT'
  AND p.perm_code IN (
      'blog:manage_self',
      'folder:manage_self'
  );

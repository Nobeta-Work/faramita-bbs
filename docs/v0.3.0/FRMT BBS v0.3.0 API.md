# Faramita BBS v0.3.0 API

> 创建人: Nobeta  
> 更新人: Nobeta  
> 最后更新: 2026-06-01

本文档按当前 v0.3.0 后端 Controller、DTO、VO 整理。下列路径均不包含 Spring Boot context path，本地完整地址为 `/bbs` + API 路径。

## 1. 通用约定

### 1.1 认证

需要登录的接口在 Header 中携带：

```http
Authorization: Bearer <accessToken>
```

公开接口：

| 方法 | 路径 |
| --- | --- |
| `POST` | `/api/auth/login` |
| `POST` | `/api/auth/register` |
| `POST` | `/api/auth/refresh` |
| `GET` | `/api/users/{id}` |
| `GET` | `/api/tags` |
| `GET` | `/api/blogs/page` |
| `GET` | `/api/blogs/{id}` |

### 1.2 响应格式

普通响应：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

分页响应中的 `data`：

```json
{
  "total": 0,
  "pageNum": 1,
  "pageSize": 10,
  "pages": 0,
  "records": []
}
```

常用状态码：

| code | msg |
| --- | --- |
| `200` | 操作成功 |
| `400` | 参数或业务校验失败 |
| `401` | 未登陆，请先授权 |
| `403` | 权限不足 |
| `404` | 资源不存在 |
| `500` | 服务器异常 |

### 1.3 请求格式

- `@RequestBody` 接口使用 `application/json`。
- 文件上传使用 `multipart/form-data`。
- `@RequestParam` 使用 query 参数。
- 分页默认按 `pageNum`、`pageSize`、`sortField`、`sortOrder` 传参；`sortOrder` 支持 `asc`、`desc`。

## 2. 认证模块

### 2.1 登录

| 项 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/auth/login` |
| 认证 | 公开 |
| Content-Type | `application/json` |

请求体：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `username` | string | 是 | 4-20 位，支持字母、数字、`! @ # . _ -` |
| `password` | string | 是 | 4-20 位，支持字母、数字、`! @ # . _ -` |

成功响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `accessToken` | string | 访问令牌 |
| `refreshToken` | string | 刷新令牌 |
| `expireIn` | datetime | Access Token 过期时间 |

### 2.2 注册

| 项 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/auth/register` |
| 认证 | 公开 |
| Content-Type | `application/json` |

请求体：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `username` | string | 是 | 4-20 位，支持字母、数字、`! @ # . _ -` |
| `password` | string | 是 | 4-20 位，支持字母、数字、`! @ # . _ -` |
| `nickname` | string | 是 | 1-10 位 |
| `sex` | integer | 否 | `0` 未知，`1` 男，`2` 女；默认 `0` |
| `race` | string | 否 | 1-10 位；默认值以后端常量为准 |

成功响应：`data = null`。

### 2.3 刷新令牌

| 项 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/auth/refresh` |
| 认证 | 公开 |
| 参数位置 | Query |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `refreshToken` | string | 是 | 刷新令牌 |

成功响应 `data` 同登录接口。刷新成功后旧 Refresh Token 失效。

### 2.4 登出

| 项 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/auth/logout` |
| 认证 | Bearer Token |
| Content-Type | 无请求体 |

成功响应：`data = null`。服务端会将当前 Access Token 加入黑名单，并清理当前用户的 Refresh Token 和登录缓存。

## 3. 用户模块

### 3.1 获取当前用户资料

| 项 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/users/me` |
| 认证 | Bearer Token |

成功响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 用户 ID |
| `username` | string | 登录账号 |
| `nickname` | string | 昵称 |
| `avatar` | string | 头像 |
| `sex` | integer | 性别 |
| `race` | string | 种族 |
| `signature` | string | 签名 |
| `roles` | array<string> | 角色码 |
| `createTime` | datetime | 创建时间 |

### 3.2 获取用户公开资料

| 项 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/users/{id}` |
| 认证 | 公开 |

路径参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 用户 ID |

成功响应 `data`：`id`、`nickname`、`avatar`、`sex`、`race`、`signature`、`createTime`。

### 3.3 修改当前用户资料

| 项 | 内容 |
| --- | --- |
| 方法 | `PUT` |
| 路径 | `/api/users/me` |
| 认证 | Bearer Token |
| Content-Type | `application/json` |

请求体：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `nickname` | string | 是 | 1-10 位 |
| `sex` | integer | 否 | `0`、`1`、`2` |
| `race` | string | 否 | 1-10 位 |

成功响应：`data = null`。

### 3.4 修改密码

| 项 | 内容 |
| --- | --- |
| 方法 | `PUT` |
| 路径 | `/api/users/me/password` |
| 认证 | Bearer Token |
| Content-Type | `application/json` |

请求体：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `oldPassword` | string | 是 | 4-20 位，支持字母、数字、`! @ # . _ -` |
| `newPassword` | string | 是 | 4-20 位，支持字母、数字、`! @ # . _ -` |

成功响应：`data = null`。修改成功后服务端会清理登录缓存，前端应重新登录。

### 3.5 更新头像

| 项 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/users/me/avatar` |
| 认证 | Bearer Token |
| Content-Type | `multipart/form-data` |

表单参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | file | 是 | 头像文件 |

成功响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `avatarKey` | string | 新头像 key |

## 4. 博客模块

### 4.1 查询公开博客分页

| 项 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/blogs/page` |
| 认证 | 公开，可选 Bearer Token |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `pageNum` | integer | 是 | 页码，最小 1 |
| `pageSize` | integer | 是 | 每页数量，1-100 |
| `sortField` | string | 否 | `createTime`、`updateTime`、`likeCount` |
| `sortOrder` | string | 否 | `asc` 或 `desc`，默认 `desc` |
| `keyword` | string | 否 | 标题或摘要关键词 |
| `authorId` | number | 否 | 作者 ID |
| `tagIds` | array<number> | 否 | 标签 ID 列表，可重复传 `tagIds=1&tagIds=2` |

成功响应 `data.records[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 博客 ID |
| `title` | string | 标题 |
| `summary` | string | 摘要 |
| `isPublished` | integer | 发布状态 |
| `likeCount` | integer | 点赞数 |
| `createTime` | datetime | 创建时间 |
| `updateTime` | datetime | 更新时间 |
| `author` | object | `UserBriefVO` |
| `tags` | array<object> | `TagBriefVO` 列表 |

### 4.2 创建个人博客

| 项 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/blogs/me` |
| 认证 | Bearer Token |
| Content-Type | `application/json` |

请求体：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `title` | string | 是 | 1-50 字 |
| `folderId` | number | 否 | 目录 ID，默认 `0` |

成功响应 `data`：新博客 ID。

### 4.3 查询公开博客详情

| 项 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/blogs/{id}` |
| 认证 | 公开 |

成功响应 `data`：公开博客基础字段 + `content`、`isLiked`。

### 4.4 查询个人博客详情

| 项 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/blogs/me/{id}` |
| 认证 | Bearer Token |

成功响应 `data`：公开博客基础字段 + `folderId`、`content`。

### 4.5 修改个人博客

| 项 | 内容 |
| --- | --- |
| 方法 | `PUT` |
| 路径 | `/api/blogs/me/{id}` |
| 认证 | Bearer Token |
| Content-Type | `application/json` |

请求体：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `folderId` | number | 是 | 大于等于 0 |
| `isPublished` | integer | 否 | `0` 草稿，`1` 发布 |
| `title` | string | 是 | 1-20 字 |
| `summary` | string | 否 | 最多 200 字 |
| `content` | string | 否 | Markdown 内容 |
| `tagIds` | array<number> | 否 | 标签 ID 列表 |

成功响应：`data = null`。

### 4.6 删除个人博客

| 项 | 内容 |
| --- | --- |
| 方法 | `DELETE` |
| 路径 | `/api/blogs/me/{id}` |
| 认证 | Bearer Token |

成功响应：`data = null`。

## 5. 目录模块

### 5.1 创建目录

| 项 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/folders` |
| 认证 | Bearer Token |
| Content-Type | `application/json` |

请求体：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `parentId` | number | 是 | 父目录 ID；`0` 表示逻辑根目录 |
| `name` | string | 是 | 1-20 字 |

成功响应：`data = null`。

### 5.2 重命名目录

| 项 | 内容 |
| --- | --- |
| 方法 | `PUT` |
| 路径 | `/api/folders/{id}` |
| 认证 | Bearer Token |
| Content-Type | `application/json` |

请求体：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `name` | string | 是 | 1-20 字 |

成功响应：`data = null`。

### 5.3 移动目录

| 项 | 内容 |
| --- | --- |
| 方法 | `PUT` |
| 路径 | `/api/folders/{id}/move` |
| 认证 | Bearer Token |
| Content-Type | `application/json` |

请求体：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `targetParentId` | number | 是 | 目标父目录 ID；`0` 表示逻辑根目录 |

成功响应：`data = null`。

### 5.4 删除目录

| 项 | 内容 |
| --- | --- |
| 方法 | `DELETE` |
| 路径 | `/api/folders/{id}` |
| 认证 | Bearer Token |

成功响应：`data = null`。目录下博客不会被删除，会移动到逻辑根目录。

### 5.5 获取当前用户目录树

| 项 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/folders/me` |
| 认证 | Bearer Token |

成功响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 目录 ID；逻辑根目录为 `0` |
| `name` | string | 目录名 |
| `level` | integer | 层级 |
| `children` | array<object> | 子目录 |
| `sortOrder` | integer | 排序值，v0.3.0 暂不支持修改 |

### 5.6 查询目录下博客

| 项 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/folders/{id}/blogs` |
| 认证 | Bearer Token |

Query 参数：`pageNum`、`pageSize`、`sortField`、`sortOrder`。成功响应为 `PageResult<BlogPrivateBriefVO>`。

### 5.7 批量移动博客到目录

| 项 | 内容 |
| --- | --- |
| 方法 | `PUT` |
| 路径 | `/api/folders/blogs/move` |
| 认证 | Bearer Token |
| Content-Type | `application/json` |

请求体：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `blogIds` | array<number> | 是 | 非空 |
| `targetId` | number | 是 | 目标目录 ID；`0` 表示逻辑根目录 |

成功响应：`data = null`。

## 6. Tag 模块

### 6.1 查询 Tag 分页

| 项 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/tags` |
| 认证 | 公开 |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `pageNum` | integer | 是 | 页码 |
| `pageSize` | integer | 是 | 每页数量 |
| `sortField` | string | 否 | 排序字段 |
| `sortOrder` | string | 否 | `asc` 或 `desc` |
| `keyword` | string | 否 | 标签名关键词，1-20 字 |

成功响应为 `PageResult<TagBriefVO>`，`TagBriefVO` 字段为 `id`、`name`。

### 6.2 创建 Tag

| 项 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/tags` |
| 认证 | Bearer Token |
| Content-Type | `application/json` |

请求体：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `name` | string | 是 | 1-20 字 |
| `description` | string | 否 | 1-200 字 |

成功响应 `data`：`TagBriefVO`。

## 7. 点赞模块

### 7.1 博客点赞 Toggle

| 项 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/like/blogs/{id}` |
| 认证 | Bearer Token |
| Content-Type | 无请求体 |

路径参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 博客 ID |

成功响应 `data`：当前博客最新点赞数。

后端会通过 Lua 原子切换 Redis Set 中的用户点赞状态，并向 `like:changelog:blog` Redis Stream 写入 `blogId`、`userId`、`isLikeAction`、`timestamp`。数据库由定时任务异步刷新。

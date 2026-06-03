# Faramita BBS

> 一个面向创作、阅读与轻量社区互动的论坛系统。

Faramita BBS 是一个基于 Spring Boot 的社区后端项目，提供用户认证、博客发布、目录管理、标签检索、点赞互动与文件上传等能力。当前版本聚焦 v0.3.0：完成模块化后端结构、Spring Security 认证授权、Redis 缓存与点赞异步落盘链路。

## 简介

项目采用按业务域拆分的分层结构：

```text
Controller -> Service -> Mapper -> MySQL
Security Filter -> JWT -> Redis -> SecurityContext
```

主要模块包括：

- `auth`：登录、注册、刷新令牌、退出登录
- `user`：用户资料、头像、个人主页
- `blog`：博客创建、编辑、公开阅读
- `folder`：个人目录树与内容组织
- `tag`：全局标签与检索
- `like`：博客点赞、Redis Lua 原子切换、Redis Stream 异步落盘
- `file`：头像与内容图片上传

## 特性

- Spring Security + JWT 的无状态认证
- Access Token / Refresh Token 会话机制
- RBAC 用户、角色、权限模型
- Redis 缓存登录态、黑名单与点赞状态
- Redis Stream 承接点赞变更异步落盘
- MyBatis + MySQL 持久化
- 统一 `Result<T>` / `PageResult<T>` 响应格式
- SpringDoc OpenAPI 接口文档

## 技术栈

- Java 17
- Spring Boot 3.5.x
- Spring Security
- MyBatis
- MySQL
- Redis
- PageHelper
- SpringDoc OpenAPI
- Lombok

## 本地运行

准备 MySQL、Redis，并参考配置模板补齐本地配置。

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

默认访问：

- API Context Path: `http://localhost:8080/bbs`
- Swagger UI: `http://localhost:8080/bbs/swagger-ui.html`

## 构建

```powershell
.\mvnw.cmd clean package
```

跳过测试：

```powershell
.\mvnw.cmd clean package -DskipTests
```

## 文档

- [PRD](docs/FRMT%20BBS%20v0.3.0%20PRD.md)
- [API](docs/FRMT%20BBS%20v0.3.0%20API.md)

## 版本

当前目标版本：`v0.3.0`


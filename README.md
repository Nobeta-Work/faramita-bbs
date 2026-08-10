<p align="center">
    <img src="https://nobeta.cn/gallery/files/2026/06/11/6b527c296c394582bf664c2d58a7f574.webp" alt="Para BBS" width="100%">
</p>

# 彼记 / Para BBS

<p align="center">
    <a href="https://nobeta.cn/bbs"><img src="https://nobeta.cn/gallery/files/2026/06/11/78a9d681b8354b3ba316343e4e3c6261.webp" width="30%"/></a>
    <br>
    <a href="https://nobeta.cn/bbs">Para BBS</a> | <a href="https://nobeta.cn/bbs/blog/40"><em>v0.3.1</em></a>
</p>

彼记（Para BBS）是一个基于 Java 后端开发学习链路，不断更新、迭代、维护的云笔记/论坛项目。

以 "实现为先，迭代最佳"、"框架优先，复用轮子" 为代码开发标准，尽可能保持项目代码的质量。


| 后端技术栈                       | 作用                   |
| ---------------------------- | ---------------------- |
| **Spring Boot / Spring MVC** | Web 开发的后端基本框架 |
| **MySQL**                    | 数据库                 |
| **MyBatis**                  | 服务端与数据库的中间件 |
| **Spring Security**          | 认证授权系统           |
| **Redis**                    | 缓存                   |
| **Docker**                   | 打包部署               |

---

## 功能模块

主要模块包括：

- `auth`：登录、注册、刷新令牌、退出登录
- `user`：用户资料、头像、个人主页
- `blog`：博客创建、编辑、公开阅读
- `folder`：个人目录树与内容组织
- `tag`：全局标签与检索
- `like`：博客点赞、Redis Lua 原子切换、Redis Stream 异步落盘
- `file`：头像与内容图片上传

## 本地运行

准备 MySQL、Redis，并参考 `backend/bbs/src/main/resources/application-dev.yml.example` 补齐本地配置。

```powershell
cd backend/bbs
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

默认访问：

- 前端路径: `http://localhost:5173/bbs`
- 后端 API : `http://localhost:8080/bbs`

*支持 Docker 容器化部署，详情参考 [Docker 部署](docs/DOCKER.md) 。*

---

## 贡献

- **[Github | Para BBS](https://github.com/Nobeta-Work/parabbs)**

欢迎你在 Github 仓库提交 [Issues](https://github.com/Nobeta-Work/parabbs/issues) ，但并不推荐你提交 PR；若有相关问题，可以发送邮件联系 📧 `qw1450975458@163.com` 。

- **[Para BBS | Para BBS](https://nobeta.cn/bbs/blog/2064990305689534464)**

## 社区

- [Para BBS](https://nobeta.cn/bbs)
- [Issues](https://github.com/Nobeta-Work/parabbs/issues)

## License

MIT — 允许任意 fork，自由使用。

Built by [Para@Nobeta](https://nobeta.cn/bbs/1) | [Github@Nobeta-Work](https://github.com/Nobeta-Work) .

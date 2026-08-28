# Docker 部署指南

Para BBS 支持通过 Docker 容器化部署，提供三种编排模式，覆盖不同部署场景。

---

## 目录

- [架构概览](#架构概览)
- [前置准备](#前置准备)
- [快速开始（全栈部署）](#快速开始全栈部署)
- [场景一：仅部署后端](#场景一仅部署后端)
- [场景二：部署前端 + 后端](#场景二部署前端--后端)
- [场景三：只编排中间件](#场景三启动中间件)
- [环境变量说明](#环境变量说明)
- [注意事项](#注意事项)
- [常用命令](#常用命令)

---

## 架构概览

```
                    ┌──────────────────────┐
                    │   Frontend (Nginx)   │
                    │   port: 80           │
                    │   network: host      │
                    └──────┬───────────────┘
                           │ proxy_pass → localhost:8080
                           ▼
┌──────────┐     ┌──────────────────┐     ┌──────────┐
│  MySQL   │◄────│  Backend (Java)  │────►│  Redis   │
│  :3306   │     │  port: 8080      │     │  :6379   │
└──────────┘     └──────────────────┘     └──────────┘
      ▲                                       ▲
      └─────────── 桥接网络 ──────────────────┘
           服务名互访: mysql → redis
```

**网络说明：**

- **backend / mysql / redis**：在 Docker Compose 默认的桥接网络中，通过服务名（`mysql`、`redis`）互访。
- **frontend**：使用 `network_mode: host`，直接绑定宿主机网络，Nginx 通过 `localhost:8080` 反向代理后端 API。

> **注意**：`network_mode: host` 仅在 Linux 宿主机上完全生效。在 **Docker Desktop（Windows/Mac）** 上，host 网络映射到的是 Docker Desktop 虚拟机而非宿主机，请改为端口映射模式或直接使用宿主机 IP。

---

## 前置准备

### 环境要求

| 组件 | 版本要求 |
|------|----------|
| Docker | ≥ 24.0 |
| Docker Compose | ≥ v2.20 |
| JDK | 17（用于本地打包） |
| Maven | 3.8+（或使用 `./mvnw`） |
| Node.js | 18+（用于前端构建） |

### 项目结构

```
Para BBS/
├── infra/docker/
│   ├── backend/
│   │   ├── Dockerfile          # 后端镜像构建
│   │   ├── .env                # 后端运行环境变量（不提交到 Git）
│   │   └── .env.example        # 环境变量模板
│   ├── frontend/
│   │   ├── Dockerfile          # 前端镜像构建
│   │   └── nginx.conf          # Nginx 反向代理配置
│   ├── docker-compose.yml             # 全栈编排
│   ├── docker-compose.backend.yml     # 仅后端 + MySQL + Redis
│   └── docker-compose.frontend.yml    # 前端 + 后端
├── infra/mysql/
│   ├── full_infra.sql                 # 全新部署的完整数据库结构
│   └── v0.4.0-refactor.sql            # 既有 schema 的 v0.4.0 重命名脚本
├── frontend/web/                      # 前端工程
├── backend/bbs/                       # 后端工程
├── .dockerignore               # Docker 构建忽略规则
└── .gitignore
```

---

## 快速开始（全栈部署）

> 适用场景：从零开始在单机上部署完整服务（前端 + 后端 + MySQL + Redis）。

### 步骤 1：构建前端静态资源

在项目根目录执行：

```bash
cd frontend/web
npm install
npm run build
cp -r dist/ ../../infra/docker/frontend/
cd ../..
```

### 步骤 2：打包后端 JAR

在项目根目录执行：

```bash
cd backend/bbs
./mvnw clean package -DskipTests
cp target/bbs.jar ../../infra/docker/backend/
cd ../..
```

### 步骤 3：配置环境变量

```bash
cp infra/docker/backend/.env.example infra/docker/backend/.env
```

编辑 `infra/docker/backend/.env`，填写以下必填项：

| 变量 | 说明 | 示例值 |
|------|------|--------|
| `PARA_DATASOURCE_PASSWORD` | MySQL root 密码 | 自行设定 |
| `PARA_REDIS_PASSWORD` | Redis 密码 | 自行设定 |
| `PARA_JWT_SECRET` | JWT 签名密钥（≥256bit） | 生成随机字符串 |
| `PARA_GITHUB_TOKEN` | Github Token（博客图片上传） | `ghp_xxx` |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | 与上方一致 |
| `REDIS_PASSWORD` | Redis 密码 | 与上方一致 |

### 步骤 4：启动所有服务

全栈编排仅将 `infra/mysql/full_infra.sql` 挂载到 MySQL 初始化目录。`v0.4.0-refactor.sql` 仅供已有数据库手动迁移使用，不会在全新部署时自动执行。

```bash
docker compose -f infra/docker/docker-compose.yml up -d
```

### 步骤 5：验证

```bash
# 检查容器状态
docker compose -f infra/docker/docker-compose.yml ps

# 检查后端健康
curl http://localhost:8080/bbs/api/hello
```

启动完成后访问 `http://localhost/bbs` 即可进入论坛首页。

---

## 场景一：仅部署后端

> 适用场景：MySQL 和 Redis 已在宿主机或外部运行，仅容器化后端 Java 程序。

### 步骤 1：打包后端 JAR

```bash
cd backend/bbs
./mvnw clean package -DskipTests
cp target/bbs.jar ../../infra/docker/backend/
cd ../..
```

### 步骤 2：配置环境变量

```bash
cp infra/docker/backend/.env.example infra/docker/backend/.env
```

编辑 `infra/docker/backend/.env`，将数据库和 Redis 连接地址指向外部服务：

```ini
PARA_DATASOURCE_URL=jdbc:mysql://<外部MySQL IP>:3306/para_bbs?...
PARA_REDIS_HOST=<外部Redis IP>
```

### 步骤 3：启动

```bash
docker compose -f infra/docker/docker-compose.backend.yml up -d
```

---

## 场景二：部署前端 + 后端

> 适用场景：MySQL 和 Redis 已在外部运行，容器化前端 Nginx + 后端 Java。

### 步骤 1：构建前端

```bash
cd frontend/web
npm install && npm run build
cp -r dist/ ../../infra/docker/frontend/
cd ../..
```

### 步骤 2：打包后端

```bash
cd backend/bbs
./mvnw clean package -DskipTests
cp target/bbs.jar ../../infra/docker/backend/
cd ../..
```

### 步骤 3：配置环境变量

```bash
cp infra/docker/backend/.env.example infra/docker/backend/.env
```

编辑 `infra/docker/backend/.env`，将数据库和 Redis 指向外部服务。

### 步骤 4：启动

```bash
docker compose -f infra/docker/docker-compose.frontend.yml up -d
```

启动后访问 `http://localhost/bbs`。

---
## 场景三：启动中间件

启动 Redis

```bash
docker compose -f infra/docker/docker-compose.yml up -d redis
```

访问面板 `http://localhost:15672`

启动 Elasticsearch + Kibana

```bash
docker compose -f infra/docker/docker-compose.yml up -d elasticsearch kibana
```

启动 RabbitMQ

```bash
docker compose -f infra/docker/docker-compose.yml up -d rabbitmq
```


## 环境变量说明

后端容器通过 `env_file: infra/docker/backend/.env` 注入以下环境变量，运行时覆盖 `application.yml` 中的 Spring 占位符。

### 必填项

| 变量 | 映射配置 | 说明 |
|------|----------|------|
| `SPRING_PROFILES_ACTIVE` | `spring.profiles.active` | 激活的 profile（`dev` / `prod`） |
| `PARA_DATASOURCE_URL` | `para.datasource.url` | MySQL JDBC 连接 URL，容器内使用 `mysql:3306` |
| `PARA_DATASOURCE_USERNAME` | `para.datasource.username` | 数据库用户名 |
| `PARA_DATASOURCE_PASSWORD` | `para.datasource.password` | 数据库密码 |
| `PARA_REDIS_HOST` | `para.redis.host` | Redis 主机，容器内使用 `redis` |
| `PARA_REDIS_PORT` | `para.redis.port` | Redis 端口 |
| `PARA_REDIS_PASSWORD` | `para.redis.password` | Redis 密码 |
| `PARA_JWT_SECRET` | `para.jwt.secret` | JWT HMAC-SHA256 签名密钥 |
| `PARA_FILE_AVATAR_ROOT_PATH` | `para.file.avatar.root-path` | 头像存储路径 |

### 可选配置

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `PARA_REDIS_DATABASE` | Redis 数据库编号 | `0` |
| `PARA_REDIS_CONNECT_TIMEOUT` | Redis 连接超时 | `3000ms` |
| `PARA_REDIS_TIMEOUT` | Redis 命令超时 | `3000ms` |
| `PARA_FILE_AVATAR_MAX_SIZE` | 头像文件最大字节数 | `10485760`（10MB） |
| `PARA_FILE_IMAGE_MAX_SIZE` | 图片文件最大字节数 | `15728640`（15MB） |
| `PARA_JWT_ACCESS_TOKEN_EXPIRE` | Access Token 过期时间（ms） | `864000000`（10天） |
| `PARA_JWT_REFRESH_TOKEN_EXPIRE` | Refresh Token 过期时间（ms） | `864000000`（10天） |

### Compose 变量（仅全栈部署）

以下变量仅在 `docker-compose.yml` 全栈部署时使用，用于 MySQL/Redis 容器初始化：

| 变量 | 说明 |
|------|------|
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 |
| `REDIS_PASSWORD` | Redis 访问密码 |

> **注意**：Docker Compose 的变量插值（`${VAR}`）从工作目录的 `.env` 文件或 Shell 环境变量中读取。请确保执行 `docker compose` 时这些变量可用。若使用项目根目录的 `.env` 文件，请将上述变量复制其中。

---

## 注意事项

### MySQL / Redis 启动顺序

Compose 未配置 `depends_on`。MySQL 和 Redis 容器可能在后端容器启动后才完成初始化，导致后端首次连接失败。Docker 的 `restart: unless-stopped` 会在后端退出后自动重启，通常在 1-2 次重启后即可正常连接。若后端持续重启失败，请检查：

- `infra/docker/backend/.env` 中的 `PARA_DATASOURCE_URL` 是否正确使用了服务名 `mysql`
- MySQL 容器日志：`docker logs parabbs-mysql`

### 网络模式（host vs bridge）

- **Linux 宿主机**：`network_mode: host` 正常工作，前端可直接通过 `localhost:8080` 访问后端。
- **Docker Desktop（Windows/Mac）**：host 网络不可用，需将前端容器的 `network_mode: host` 替换为端口映射，并修改 `nginx.conf` 中 `proxy_pass` 的目标地址为宿主机 IP。

### 数据持久化

全栈部署创建了三个 Docker Volume，数据不会随容器删除而丢失：

| Volume | 挂载路径 | 用途 |
|--------|----------|------|
| `mysql_data` | `/var/lib/mysql` | MySQL 数据库文件 |
| `redis_data` | `/data` | Redis 持久化数据 |
| `avatar_data` | `/data/parabbs/avatar` | 用户头像文件 |

### 端口占用

请确保以下端口在启动前未被占用：

| 端口 | 服务 |
|------|------|
| `80` | 前端 Nginx |
| `8080` | 后端 Spring Boot |
| `3306` | MySQL |
| `6379` | Redis |

### 生产环境部署

生产环境部署建议额外关注以下事项：

1. **激活 prod profile**：设置 `SPRING_PROFILES_ACTIVE=prod`，并确保 `application-prod.yml` 配置正确。
2. **关闭 Swagger**：prod profile 中已默认关闭，确认 `springdoc.api-docs.enabled=false`。
3. **更换密钥**：务必更换 `PARA_JWT_SECRET` 为高强度随机密钥（≥256bit）。
4. **MySQL 和 Redis 密码**：使用强密码，避免使用 `change_me`。
5. **MySQL 端口**：建议不对外暴露 `3306` 端口，移除 `ports` 中的 `3306:3306` 映射。
6. **HTTPS**：建议在前端 Nginx 前挂载反向代理（如 Caddy / Nginx Proxy Manager）以提供 SSL 终端。

---

## 常用命令

```bash
# 查看所有容器状态
docker compose -f infra/docker/docker-compose.yml ps

# 查看后端日志
docker logs -f parabbs-backend

# 查看前端日志
docker logs -f parabbs-frontend

# 重启后端
docker compose -f infra/docker/docker-compose.yml restart backend

# 停止所有服务
docker compose -f infra/docker/docker-compose.yml down

# 停止服务并删除数据卷（⚠️ 会清空数据库）
docker compose -f infra/docker/docker-compose.yml down -v

# 重新构建并启动（代码更新后）
docker compose -f infra/docker/docker-compose.yml up -d --build

# 进入 MySQL 容器
docker exec -it parabbs-mysql mysql -uroot -p

# 进入 Redis 容器
docker exec -it parabbs-redis redis-cli
```

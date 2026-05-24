---
title: 需求文档
version: v0.2.1
description: 规范本版本 Markdown 编辑体验优化、头像本地图床重构，以及前后端接口与数据库边界。
---

# FRMT BBS v0.2.1 需求文档

## 1. 目标
本版本只解决两类问题。

1. 前端 Markdown 编辑与阅读体验对齐，补齐 Tab 缩进、表格快捷操作、代码块样式和图片标题展示。
2. 后端头像上传切换为本地存储，博客图片上传继续沿用现有图床，不改现有博客内容接口。

## 2. 范围
- 前端：博客编辑页、博客详情页、列表页、个人主页、导航栏头像展示。
- 后端：文件上传、头像更新、头像清理、用户资料接口。
- 数据库：沿用现有 `user.avatar` 和 `avatar_info` 结构，不新增表或字段。
- 不在本版本内调整：博客发布流程、登录鉴权机制、博客 CRUD、图片上传图床实现。

## 3. 业务规则
### 3.1 头像路径契约（已确认，前后端严格遵守）
- 后端存储规则：头像按日期分层存储，物理路径为 `/data/faramita-bbs/avatar/年/月/日/UUID.扩展名`
- 后端返回值：仅返回相对路径 `avatarKey`，格式严格为 `year/month/day/UUID.extension`（例如 `2026/4/18/abc123def.jpg`），不包含任何前缀、域名
- 前端拼接规则：完整可访问URL = 文件域名前缀 + `avatar/` + `avatarKey`
  - 生产环境前缀：`https://faramita.online/bbs/i/`
  - 开发环境前缀：`http://localhost:8080/bbs/i/`
- 静态资源映射：服务端 `/bbs/i/**` 直接映射到本地存储根路径 `/data/faramita-bbs/`

### 3.2 通用业务规则
- 若头像字段为空、为旧默认值（例如 `DEFAULT_AVATOR.jpg`），前端使用默认头像资源兜底。
- 博客图片上传逻辑完全不变，后端仍返回GitHub绝对URL，前端直接写入Markdown。
- 未被引用的头像文件在2小时后过期，后台每8小时清理一次。

## 4. 后端存储约束
- 头像仅存储相对路径到 `user.avatar` 字段，禁止存储完整URL或域名前缀
- 博文图片仍保持原有存储逻辑，直接存储GitHub完整URL到博客内容中
- 禁止修改现有博文图片上传接口的任何逻辑，保持100%向下兼容

## 4. 前端需求

### 4.1 编辑输入
- 编辑器按 `Tab` 时插入 4 个空格。
- 若当前有多行选区，则对选区逐行缩进。
- `Shift + Tab` 可反向取消缩进，避免破坏原有编辑状态。

### 4.2 预览同步
- 编辑态与预览态使用同一套 Markdown 渲染规则和样式变量。
- 标题、段落、列表、引用、表格、行内代码、围栏代码块、图片在两种模式下的间距和字体必须一致。
- 两种模式只允许在外壳、工具栏和交互控件上存在差异，不允许内容样式漂移。

### 4.3 表格编辑
- 现有表格插入面板保留，用于初始插入表格。
- 鼠标悬浮到表格右侧边缘时，显示 `+` 快捷入口，用于在当前列右侧新增一列。
- 鼠标悬浮到表格底部边缘时，显示 `+` 快捷入口，用于在当前行下方新增一行。
- 该快捷入口以桌面端鼠标交互为主，移动端可继续使用原有插表入口作为兜底。

### 4.4 代码块样式
- 围栏代码块按 mac 风格展示，顶部需要有独立标题栏。
- 标题栏右上角显示语言名，语言来源于代码块 fence 的 info string。
- 若未声明语言，则显示 `plain text` 或等价占位。

### 4.5 图片标题
- Markdown 语法保持 `![alt](url "title")` 不变。
- `title` 为空时，不渲染标题文本，也不预留空白占位。
- `title` 不为空时，在图片下方以小字号展示标题说明。

### 4.6 头像展示
- 所有头像展示位置统一走同一套 URL 解析逻辑。
- 个人资料页、博客列表页、博客详情页、顶部导航栏都不得再依赖头像下载接口转 Blob。
- 当头像字段为空、为绝对 URL、或为旧默认值时，走对应的统一解析规则，不得在页面里各自拼接。

## 5. 后端需求

### 5.1 文件类型与大小
| 类型 | 接口 | 存储方式 | 允许 MIME | 最大值 | 返回值 |
|---|---|---|---|---|---|
| 头像 | `POST /api/uploadAvatar`、`POST /api/{uid}/upload/avatar` | 本地磁盘 | `image/jpeg`、`image/png`、`image/gif`、`image/webp`、`image/jpg` | 10MB | 相对路径 key |
| 博客图片 | `POST /api/uploadImage` | GitHub 图床 | `image/jpeg`、`image/png`、`image/gif`、`image/webp`、`image/jpg` | 15MB | 绝对 URL |

### 5.2 头像上传规则
- 头像文件保存在本地目录，目录层级按日期组织，格式为 `avatar/YYYY/M/D/uuid.ext`。
- 公共上传接口只负责落盘并写入 `avatar_info`，不绑定用户。
- 用户头像更新接口在落盘后，将头像与当前用户绑定，并更新 `user.avatar`。
- 已绑定头像视为引用资源，不应被清理任务删除。

### 5.3 头像清理规则
- 系统只清理 `is_referenced = 0` 且 `expire_time < 当前时间` 的头像。
- 清理时需要同时删除磁盘文件与数据库记录。
- 清理任务执行失败时不能影响正常业务请求。

## 6. 接口定义
说明：下文路径默认包含服务前缀 `/bbs`，文档仅写 `api/...` 部分。

### 6.1 文件接口
| 接口 | 方法 | 鉴权 | 请求参数 | 响应 | 说明 |
|---|---|---|---|---|---|
| `api/uploadAvatar` | `POST` | 否 | `multipart/form-data`，`file: MultipartFile` | `Result<String>`，`data` 为头像相对路径 | 供注册页等未登录场景使用 |
| `api/uploadImage` | `POST` | 否 | `multipart/form-data`，`file: MultipartFile` | `Result<String>`，`data` 为博客图片绝对 URL | 供 Markdown 编辑器插图使用 |
| `api/downloadAvatar` | `GET` | 否 | `avatar: string` | 不保留 | 本版本删除，不允许前端继续调用 |
| `api/{uid}/upload/avatar` | `POST` | 是 | 路径参数 `uid: Long`，`multipart/form-data`，`file: MultipartFile` | `Result<String>`，`data` 为头像相对路径 | 供已登录用户修改头像 |

### 6.2 用户接口
| 接口 | 方法 | 鉴权 | 请求参数 | 响应 | 说明 |
|---|---|---|---|---|---|
| `api/login` | `POST` | 否 | JSON `LoginDTO`：`username`、`password` | `Result<LoginVO>` | 登录后返回 token 和用户基础信息 |
| `api/register` | `POST` | 否 | JSON `RegisterDTO`：`username`、`password`、`nickname`、`sex`、`race`、`avatar` | `Result<Void>` | `avatar` 可为空，保存为头像 key 或默认值 |
| `api/{uid}` | `GET` | 否 | 路径参数 `uid: Long` | `Result<ProfileVO>` | 查询个人资料页 |
| `api/{uid}/current` | `GET` | 是 | 路径参数仅用于路由，实际用户由 token 决定 | `Result<LoginVO>` | 前端固定请求 `uid = 0` |
| `api/{uid}/profile` | `PUT` | 是 | 路径参数 `uid: Long`，JSON `ProfileDTO`：`id`、`password`、`nickname`、`sex`、`race`、`avatar`、`signature` | `Result<Void>` | 仅允许本人修改 |

### 6.3 通用返回约定
- 成功响应统一使用 `Result.success(...)`，即 `code = 1`。
- 失败响应统一走 `Result.error(...)` 或 HTTP `401`。
- 前端对 `401` 视为登录失效，需清空 token 并跳转登录页。
- 文件上传类接口常见错误包括：空文件、超大小、类型错误、文件写入失败。

## 7. 数据库
本版本不新增数据库表，也不新增字段。

- `user.avatar` 继续作为头像引用字段使用，存储相对 key 或历史绝对 URL。
- `avatar_info` 继续作为头像生命周期表使用，字段保持 `id`、`file_uuid`、`is_referenced`、`user_id`、`expire_time`。
- 由于没有结构变更，本版本不提供迭代 SQL。

## 8. 验收标准
- 编辑器按 Tab 后能稳定插入 4 个空格。
- 编辑态和预览态的 Markdown 内容样式一致。
- 表格右边和下边的 `+` 快捷入口可用。
- 围栏代码块显示语言标签。
- 图片标题按规则显示或隐藏。
- 头像不再依赖下载接口，所有展示位能直接渲染。
- 博客图片上传仍然可用，返回值可直接写入 Markdown。
- 数据库无需执行额外迁移 SQL。

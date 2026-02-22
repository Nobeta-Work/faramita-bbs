# Faramita BBS（初渡 v0.2.0）

一个前后端分离的笔记/论坛类开源项目。

## 项目结构

本仓库当前仅维护并发布以下两个主目录：

- `Backend/`：Java + Spring Boot 后端
- `Frontend/`：Vue 3 + TypeScript + Vite 前端

其余目录（如历史备份、版本日志、设计草稿等）默认不参与发布。

## 技术栈

- 后端：Java 17、Spring Boot、MyBatis、MySQL、JWT
- 前端：Vue 3、TypeScript、Vite、Pinia、Naive UI

## 本地运行

### 1) 启动后端

进入 `Backend/bbs`，执行：

```bash
./mvnw spring-boot:run
```

Windows 下可使用：

```bash
mvnw.cmd spring-boot:run
```

### 2) 启动前端

进入 `Frontend/faramita-bbs-frontend`，执行：

```bash
npm install
npm run dev
```

## 环境变量（安全）

为避免泄露密钥，后端配置已改为从环境变量读取：

- `FARAMITA_JWT_SECRET`
- `FARAMITA_GITHUB_TOKEN`
- `FARAMITA_DB_URL`
- `FARAMITA_DB_USERNAME`
- `FARAMITA_DB_PASSWORD`

请在本地或部署环境中注入上述变量，不要将真实密钥提交到仓库。

## 开源许可

本项目遵循 MIT License，详情见 `LICENSE`。

---

欢迎提交 Issue / PR，一起把 Faramita BBS 做得更好。

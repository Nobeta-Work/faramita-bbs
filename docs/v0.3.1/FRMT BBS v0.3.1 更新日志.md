### 更新目标

- 补齐日志提示
- 补齐单元测试
- 优化前端编辑与若干 v0.3.0 bug

### 主要更新内容

| 变更内容 | 变更类型 | 模块/页面 | 具体描述 |
| :-: | :-: | :-: | :-: |
| 自动保存 | 新增功能 | 博客编辑页 | 编辑页面的工具栏，除了保存、删除外，增加一个 `Autosave` 开关，默认关闭，启动后如有修改，1分钟后发起保存一次。(注意逻辑，防止大量自动保存请求打到后端。) |
| 括号类编辑体验 | 新增功能 | 博客编辑页 |  增强编辑器体验：`()` 位于`)` 之前输入 `)` 改为跳出右括号而非再打一个右括号，\` `]` `$` `}` 等同理，在右括号的右边可以追加。|
| 编辑模式切换 | 新增功能 | 博客编辑页 | 对编辑页面的工具栏：增加切换编辑模式（两档，所见即所得和分屏预览）|
| 前端认证刷新 | 修复问题 | 前端认证 | (1) 无感刷新，根据 `expireIn` 在请求前审查，如果过期主动刷新令牌；(2) 没有主动刷新，收到401报错后先 refreshToken 重试而不是立即显式报错401。刷新令牌失败再前端显式报错。|
| 字数统计位置偏移 | 修复问题 | 博客编辑页 | 字数统计当前位置遮挡底部编辑区域。将字数统计移到可编辑区域底部。|
| 一键到顶 | 新增功能 | 博客公开详情页 | 在public阅览界面，也应该增加一键到顶的按钮。|
| 日志补齐 | 新增功能 | 审计 | 补齐日志切面 |
| 单元测试 | 新增功能 | 测试 | 补齐后端单元测试 |

#### 日志补齐

标准日志为 JSON 约束。

JSON 示例如下，其中 `TIME`, `TYPE`, `OPERATOR`, `MODULE` 通过 AOP 默认织入，`MESSAGE`, `DATA` 为可选字段，DATA 可传入 JSON 序列化对象。

标准化输出：`TIME|INFO|OPERATOR|MODULE [-m "MESSAGE"] [: DATA]`

```json

{
    "TIME": "2026-6-4 16:23:15",
    "TYPE": "INFO",
    "OPERATOR": "user<1>/anoymous/system",
    "MODULE": "AuthController.login",
    "MESSAGE": "用户登陆",
    "DATA": {
        "username": "123456"
    }
}

```

标准化输出示例： `2026-6-4 16:23:15|INFO|user<1>|AuthController.login -m "用户登陆" : {"username":"123456"}`

值得注意的是，在 v0.3.1 版本中，按照框架优先的原则，采用 Lombok 的 `log` 记录。直接打印格式为：

```bash

2026-06-04T16:23:15.823+08:00  INFO 12345 --- [bbs] [           main] o.f.b.common.util.AuditLogUtil        : 2026-6-4 16:23:15|INFO|user<1>|AuthController.login -m "用户登陆" : {"username":"123456"}

```

为了减少日志冗余，去除 `TIME` 和 `TYPE` 字段，修改为：

```bash

2026-06-04T16:23:15.823+08:00  INFO 12345 --- [bbs] [           main] o.f.b.common.util.AuditLogUtil        : user<1>|AuthController.login -m "用户登陆" : {"username":"123456"}

```

### 单元测试

遵循 "框架优先，复用轮子" 原则，使用 Spring Starter Test 内置的：
- 测试执行引擎 `JUnit5`
- 依赖模拟 `Mockito`
- 流式断言 `AssertJ`
- Spring 测试上下文 `Spring Test`
- Web 层测试 `MockMvc`

#### 规范

**1. 目录与命名**
- 测试类命名：`被测类名 + Test`，例如 `AuthControllerTest`
- 测试方法命名：`场景_操作_预期结果` (given_when_then 风格)

**2. 分层测试**
- Controller 层：切片测试，使用 `@WebMvcTest` 切片配合 Mockito 模拟请求。
- Service 层：纯单元测试，完全脱离 Spring 容器，使用 Mockito 模拟依赖。

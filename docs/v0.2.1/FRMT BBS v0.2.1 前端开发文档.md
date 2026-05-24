# FRMT BBS v0.2.1 前端开发文档

## 1. 目标
本版本前端只做两类改造。

1. 统一 Markdown 编辑与预览体验，补齐 `Tab` 缩进、表格快捷入口、代码块语言标签、图片标题展示。
2. 统一头像展示解析逻辑，所有头像位置直接使用头像 key 解析后的 URL，不再走头像下载 Blob。

## 2. 前端改动范围
### 2.1 页面与组件
- `src/views/BlogDetailView.vue`
- `src/views/BlogListView.vue`
- `src/views/UserProfile.vue`
- `src/components/TopNav.vue`
- `src/views/RegisterView.vue` 仅保留上传预览，不改上传接口

### 2.2 公共工具
- 新增头像 URL 解析工具
- 新增 Markdown 渲染后装饰工具

### 2.3 样式
- `src/styles/global.scss`
- `src/views/BlogDetailView.vue` 的 scoped 样式

### 2.4 接口层
- `src/api/file.ts`
- 仅保留 `uploadAvatar`、`uploadImage`
- 删除前端对 `downloadAvatar` 的依赖

## 3. 修改位置确认
| 位置 | 作用 | 处理方式 |
|---|---|---|
| `src/views/BlogDetailView.vue` | Markdown 编辑主页面 | 处理 `Tab`、表格 `+` 入口、编辑/预览同步、代码块/图片装饰 |
| `src/views/BlogListView.vue` | 博客列表头像展示 | 改为直接解析头像 key |
| `src/views/UserProfile.vue` | 个人页头像展示 | 改为直接解析头像 key |
| `src/components/TopNav.vue` | 顶部导航头像展示 | 改为直接解析头像 key |
| `src/utils/avatar.ts` | 头像统一解析 | 统一处理默认头像、相对 key、绝对 URL |
| `src/utils/markdown.ts` | Markdown DOM 装饰 | 统一处理代码块语言、图片标题 |
| `src/styles/global.scss` | Markdown 共享样式 | 统一编辑态/预览态内容样式 |

## 4. 统一头像规则
### 4.1 解析规则
新增 `resolveAvatarUrl(avatar)`，输入可接受：

- 空值
- 旧默认值
- 相对 key
- 绝对 URL
- `blob:` / `data:` 临时地址

返回值规则：

- 空值或旧默认值，返回本地默认头像资源
- 绝对 URL、`blob:`、`data:`，原样返回
- 相对 key，拼接为文件域名前缀 + `avatar/` + key

### 4.2 默认前缀
- 开发环境：`http://localhost:8080/bbs/i/`
- 生产环境：`https://faramita.online/bbs/i/`

### 4.3 使用位置
- 顶部导航栏头像
- 博客列表作者头像
- 博客详情页作者头像
- 个人主页头像

## 5. Markdown 编辑器改造
### 5.1 `Tab` / `Shift + Tab`
要求：

- `Tab` 插入 4 个空格
- 多行选区逐行缩进
- `Shift + Tab` 逐行反缩进

实现建议：

- 在 `BlogDetailView.vue` 中对 Vditor 编辑区域做键盘拦截
- 使用同一套选区处理逻辑处理单行和多行
- 处理完成后同步编辑内容与 TOC 状态

### 5.2 表格快捷入口
要求：

- 保留现有表格插入面板
- 桌面端在表格右边缘显示 `+`，用于在当前列右侧新增一列
- 桌面端在表格底边缘显示 `+`，用于在当前行下方新增一行
- 移动端继续只用原有插表面板

实现建议：

- 仅对编辑态生效
- 通过 hover 计算当前单元格、行、列位置
- `+` 按钮直接操作当前表格 DOM
- 操作后触发内容同步，避免保存内容与视图不一致

### 5.3 代码块样式
要求：

- 围栏代码块展示为 mac 风格卡片
- 顶部独立标题栏
- 右上角显示语言名
- 未声明语言时显示 `plain text`

实现建议：

- 在渲染后的 `pre > code` 上补充语言数据属性
- 样式统一由 `global.scss` 和 BlogDetail 的内容样式控制
- 编辑态和预览态使用同一套代码块样式

### 5.4 图片标题
要求：

- Markdown 语法保持 `![alt](url "title")`
- `title` 为空时不展示标题，也不保留空白
- `title` 非空时在图片下方显示小字说明

实现建议：

- 只对渲染后的图片容器补充标题数据属性
- 通过 CSS 显示标题文本
- 不修改原始 Markdown 语法

### 5.5 编辑/预览同步
要求：

- 编辑态与预览态使用同一套 Markdown 渲染结果和样式变量
- 标题、段落、列表、引用、表格、代码块、图片的间距和字体保持一致
- 外壳、工具栏、交互控件可以不同，内容样式不能漂移

实现建议：

- 把内容样式从“页面态差异”改成“共享内容样式”
- 删除编辑态专属的紧凑字体/行距覆盖
- 预览态与编辑态都走同一套装饰函数

## 6. 建议的实现顺序
1. 新增 `src/utils/avatar.ts`
2. 新增 `src/utils/markdown.ts`
3. 清理 `src/api/file.ts` 中的头像下载逻辑
4. 改 `TopNav.vue`、`BlogListView.vue`、`UserProfile.vue`
5. 改 `BlogDetailView.vue`
6. 调整 `global.scss` 和 BlogDetail 的内容样式
7. 做构建验证与手工回归

## 7. 验收标准
- `Tab` 能稳定插入 4 个空格
- 多行选区能逐行缩进和反缩进
- 表格右侧和底部 `+` 入口可用，且仅桌面端展示
- 围栏代码块显示语言标签
- 图片标题按规则显示或隐藏
- 顶部导航、列表页、详情页、个人页头像都不再请求头像下载接口
- 博客图片上传逻辑不变
- `npm run build` 通过

## 8. 回归风险
- 不要修改博客图片上传接口返回值
- 不要把头像 Blob 逻辑留在页面里
- Markdown 装饰只能作用于渲染结果，不能污染原始内容
- 表格 DOM 操作后必须同步编辑内容，否则保存会丢改动
- 预览重渲染和主题切换时，装饰逻辑必须可重复执行


# 全局公共参数

**全局Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**全局Query参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**全局Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**全局认证方式**

> 无需认证

# 状态码说明

| 状态码 | 中文描述 |
| --- | ---- |
| 暂无参数 |

# Faramita彼岸

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 07:41:46

> 更新时间: 2025-10-03 07:41:46

```text
暂无描述
```

**目录Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Query参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录认证信息**

> 继承父级

**Query**

## FaramitaBBS

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 07:42:00

> 更新时间: 2025-10-03 07:42:00

```text
暂无描述
```

**目录Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Query参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录认证信息**

> 继承父级

**Query**

### 初渡v0.0.1

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 07:42:20

> 更新时间: 2025-10-03 09:28:38

```text
暂无描述
```

**目录Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Query参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录认证信息**

> 继承父级

**Query**

#### 登录API

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 07:42:51

> 更新时间: 2025-10-03 07:42:53

```text
暂无描述
```

**目录Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Query参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录认证信息**

> 继承父级

**Query**

##### 登录账号接口

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 07:50:39

> 更新时间: 2025-10-03 16:43:49

```text
暂无描述
```

**接口状态**

> 开发中

**接口URL**

> bbs/login

**请求方式**

> POST

**Content-Type**

> form-data

**请求Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| username | 377877931 | string | 是 | 账号 |
| password | 153246789#faramita | string | 是 | 密码 |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**Query**

##### 注册账号接口

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 07:52:12

> 更新时间: 2025-10-03 16:41:50

```text
暂无描述
```

**接口状态**

> 开发中

**接口URL**

> bbs/register

**请求方式**

> POST

**Content-Type**

> form-data

**请求Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| username | 377877931 | string | 是 | 账号 |
| password | 153246789#faramita | string | 是 | 密码 |
| sex | 2 | integer | 是 | 性别 0=>女 1=>男 2=>神秘 |
| race | 哥布林 | string | 是 | 种族 |
| avator | ${url} | string | 是 | 头像URL |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**Query**

##### 根据token查询用户详情

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-06 13:43:04

> 更新时间: 2025-10-06 13:43:52

```text
暂无描述
```

**接口状态**

> 开发中

**接口URL**

> localhost:8080/bbs/0/current

**请求方式**

> GET

**Content-Type**

> none

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEsImV4cCI6MTc1OTgxNTI2OH0.4UGBKSAkRFvKfzQhrwD1sJgVxOeAk8JS-aSjaYcn8Sk | string | 是 | - |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEsImV4cCI6MTc1OTgxNTI2OH0.4UGBKSAkRFvKfzQhrwD1sJgVxOeAk8JS-aSjaYcn8Sk | string | 是 | - |

**Query**

#### 个人资料展示API

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 07:54:05

> 更新时间: 2025-10-03 07:54:05

```text
暂无描述
```

**目录Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Query参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录认证信息**

> 继承父级

**Query**

##### 获取个人资料

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 07:58:24

> 更新时间: 2025-10-06 13:46:46

###### 获取个人资料

**昵称
性别
种族
个性签名
图片
动态**

###### 获取动态

**博文信息*3**

**接口状态**

> 开发中

**接口URL**

> bbs/api/{uid}

**请求方式**

> GET

**Content-Type**

> none

**路径变量**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| uid | - | string | 是 | - |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**Query**

##### 修改个人资料接口

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 10:46:23

> 更新时间: 2025-10-06 06:43:10

###### 权限控制

**需登录(请求头携带Token)
身份校验(前后端id与uid校验，后端token校验)**

**接口状态**

> 开发中

**接口URL**

> bbs/{uid}/profile

**请求方式**

> PUT

**Content-Type**

> form-data

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer {token} | string | 是 | JWT验证 |

**路径变量**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| uid | - | string | 是 | - |

**请求Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| id | 1 | number | 是 | 当前用户uid |
| nickname | 末千 | string | 是 | 昵称 |
| avator | ${url} | string | 是 | 头像URL |
| sex | 2 | integer | 是 | 性别 0=>女 1=>男 2=>神秘 |
| race | 哥布林 | string | 是 | 种族 |
| signature | hello world | string | 是 | 个性签名 |
| password | - | string | 是 | 密码 |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer {token} | string | 是 | JWT验证 |

**Query**

##### 个人资料头像更新接口

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-04 03:26:01

> 更新时间: 2025-10-06 13:48:16

```text
暂无描述
```

**接口状态**

> 开发中

**接口URL**

> /bbs/api/{uid}/upload/avatar

**请求方式**

> POST

**Content-Type**

> form-data

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEsImV4cCI6MTc1OTgxNTI2OH0.4UGBKSAkRFvKfzQhrwD1sJgVxOeAk8JS-aSjaYcn8Sk | string | 是 | - |

**路径变量**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| uid | 1 | string | 是 | - |

**请求Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| file | D:\Pictures\二次元\7h2162393p0.png | file | 是 | 头像文件,<=10MB |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEsImV4cCI6MTc1OTgxNTI2OH0.4UGBKSAkRFvKfzQhrwD1sJgVxOeAk8JS-aSjaYcn8Sk | string | 是 | - |

**Query**

#### 博客列表接口

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 10:49:21

> 更新时间: 2025-10-03 10:49:21

```text
暂无描述
```

**目录Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Query参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录认证信息**

> 继承父级

**Query**

##### 博客分页查询接口

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 11:03:10

> 更新时间: 2025-10-04 20:48:51

```text
暂无描述
```

**接口状态**

> 开发中

**接口URL**

> /bbs/{uid}/blog?page=1&pageSize=10&bigCategoryId=1&keyword=&orderBy=&sortOrder=&littleCategoryName=faramita论坛&categoryId=1

**请求方式**

> GET

**Content-Type**

> none

**请求Query参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| page | 1 | integer | 否 | 页码 |
| pageSize | 10 | integer | 否 | 每页条数 |
| bigCategoryId | 1 | integer | 否 | 大类id |
| keyword | - | string | 否 | 关键词（模糊匹配博客标题/摘要） |
| orderBy | - | string | 是 | 排序字段 |
| sortOrder | - | string | 是 | 排序方向 |
| littleCategoryName | faramita论坛 | string | 是 | 小类名 |
| categoryId | 1 | integer | 是 | 分类id |

**路径变量**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| uid | - | string | 是 | - |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
{
	"code": 200,
	"msg": "ea aliquip mollit in consequat",
	"data": {
		"currentPage": 59
	}
}
```

* 失败(404)

```javascript
暂无数据
```

**Query**

##### 博文创建接口

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 11:15:11

> 更新时间: 2025-10-04 20:35:58

```text
暂无描述
```

**接口状态**

> 开发中

**接口URL**

> /bbs/{uid}/blog/create

**请求方式**

> POST

**Content-Type**

> form-data

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer {token} | string | 是 | JWT验证 |

**路径变量**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| uid | - | string | 是 | - |

**请求Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| title | 标题 | string | 是 | 标题 |
| content | ${html} | string | 是 | 博文内容(Markdown源码，长度限制1-100000字符) |
| summary | 摘要 | string | 否 | 未传递时自动截取content前150字符；1-200字符 |
| bigCategoryId | 1 | integer | 是 | 大类id |
| littleCategoryName | faramita论坛 | string | 是 | 小类名 |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer {token} | string | 是 | JWT验证 |

**Query**

##### 博文查询接口

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 11:19:22

> 更新时间: 2025-10-04 23:47:14

```text
暂无描述
```

**接口状态**

> 开发中

**接口URL**

> /bbs/{uid}/blog/{bloguid}

**请求方式**

> GET

**Content-Type**

> none

**路径变量**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| uid | - | string | 是 | 被查询用户id |
| bloguid | - | string | 是 | - |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**Query**

##### 删除博文

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 11:31:48

> 更新时间: 2025-10-03 12:24:34

```text
暂无描述
```

**接口状态**

> 开发中

**接口URL**

> /bbs/{uid}/blog/{blogId}

**请求方式**

> DELETE

**Content-Type**

> none

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer {token} | string | 是 | JWT验证 |

**路径变量**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| uid | - | string | 是 | - |
| blogId | - | string | 是 | - |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer {token} | string | 是 | JWT验证 |

**Query**

##### 博文更新接口

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 11:31:51

> 更新时间: 2025-10-05 01:07:11

```text
暂无描述
```

**接口状态**

> 开发中

**接口URL**

> /bbs/{uid}/blog/{blogId}

**请求方式**

> PUT

**Content-Type**

> form-data

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEsImV4cCI6MTc1OTY2ODY0NX0.PD4CWEfU_N-s-0njRLJcAlWNm8iD4ARqbOZqv5IaA_w | string | 是 | JWT验证 |

**路径变量**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| uid | 1 | string | 是 | - |
| blogId | 175958663851a25a9b-f86e-4e8c-b6cf-ddbf1b4731d7 | string | 是 | - |

**请求Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| title | 这是新的标题 | string | 否 | 标题 |
| content | - | string | 否 | 博文内容(Markdown源码，长度限制1-100000字符) |
| summary | - | string | 否 | 未传递时自动截取content前150字符；1-200字符 |
| littleCategoryName | Faramita Games | string | 是 | 小类查询名(不支持大类修改) |
| bigCategoryId | 1 | integer | 是 | 大类id |
| isPublished | 1 | string | 是 | 发布 |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**请求Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| Authorization | Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEsImV4cCI6MTc1OTY2ODY0NX0.PD4CWEfU_N-s-0njRLJcAlWNm8iD4ARqbOZqv5IaA_w | string | 是 | JWT验证 |

**Query**

#### 文件传输API

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 13:22:12

> 更新时间: 2025-10-03 13:28:28

```text
暂无描述
```

**目录Header参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Query参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| 暂无参数 |

**目录认证信息**

> 继承父级

**Query**

##### 头像上传接口

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 13:28:18

> 更新时间: 2025-10-04 05:40:35

```text
暂无描述
```

**接口状态**

> 开发中

**接口URL**

> /bbs/uploadAvatar

**请求方式**

> POST

**Content-Type**

> form-data

**请求Body参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| file | D:\Pictures\二次元\19f420b43cd005ccaab09ca96e037069f2b0d44b.jpg | file | 是 | 头像文件,<=10MB |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**Query**

##### 头像下载接口

> 创建人: Nobeta

> 更新人: Nobeta

> 创建时间: 2025-10-03 13:44:08

> 更新时间: 2025-10-04 05:40:39

```text
暂无描述
```

**接口状态**

> 开发中

**接口URL**

> /bbs/downloadAvatar?avatar=a1b2c3d4e5f6.jpg

**请求方式**

> GET

**Content-Type**

> none

**请求Query参数**

| 参数名 | 示例值 | 参数类型 | 是否必填 | 参数描述 |
| --- | --- | ---- | ---- | ---- |
| avatar | a1b2c3d4e5f6.jpg | string | 是 | 头像URL |

**认证方式**

> 继承父级

**响应示例**

* 成功(200)

```javascript
暂无数据
```

* 失败(404)

```javascript
暂无数据
```

**Query**

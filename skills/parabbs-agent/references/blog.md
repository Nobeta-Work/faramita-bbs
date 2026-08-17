- `/api/blogs/me/{id}`
method: GET
desc: get blog edit detail (include private) by blog_id.
auth: need `${pa_token}`.

- `/api/blogs/me/{id}`
method: PUT
desc: update blog.
auth: need `${pa_token}`
body:
| field | type | required | constraint |
| --- | --- | --- | --- |
| `folderId` | number | required | >= 0 |
| `isPublished` | integer | no | `0` draft，`1` publish |
| `title` | string | required | 1-20 words |
| `summary` | string | no | under 200 words |
| `content` | string | no | Markdown |
| `tagIds` | array<number> | no | tagIds |

- `/api/blogs/me`
method: POST
desc: create a blog.
auth: need `${pa_token}`
body:
| field | type | required | constraint |
| --- | --- | --- | --- |
| `title` | string | required | 1-50 chars |
| `folderId` | number | no | >= 0, default 0 |

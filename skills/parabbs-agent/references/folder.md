- `/api/folders/me`
method: GET
desc: get user's folder tree.
auth: need `${pa_token}`.

- `/api/folders/{id}/blogs?page=?&pageSize=?`
method: GET
desc: get blogs page list under a folder. Especially, the id of root folder is `0`.
auth: need `${pa_token}`.

- `/api/folders`
method: POST
desc: create a folder for the current user.
auth: need `${pa_token}`
body:
| field | type | required | constraint |
| --- | --- | --- | --- |
| `parentId` | number | required | >= 0 |
| `name` | string | required | 1-20 chars |

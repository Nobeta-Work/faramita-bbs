## 远程分支

依据前后端分离策略，远程分支组成为 `main`, `backend`, `frontend`。

- `main`: 主版本库
- `backend`: 后端开发仓库
- `frontend`: 前端开发仓库

## 本地分支

依据前后端分离策略，本地全栈开发固定保留两个分支：`backend` 与 `frontend`。

在各大版本功能开发中，后端开发使用若干 `feature/` 分支迭代，前端开发统一使用 `frontend` 分支，开发失败时单一溯源。

本地分支命名如：`feature/{version}-{feature}`，例如 `feature/v0.3.1-logging` 。

## 推送策略

后端开发完成后，各大功能分支需合并到 `backend` 分支后再推送到远程 `backend` 仓库。

```bash

git merge feature/v0.3.1-logging
git push origin HEAD:backend

```

前端开发完成后，需将 `frontend` 分支推送到远程 `frontend` 仓库。

```bash

git push origin HEAD:frontend

```

远程 `main` 主分支，需由本地 `main` 分支拉取远程 `backend` 与 `frontend` 分支后，解决冲突与版本修复，最后推送到远程 `main` 分支。

```bash

git fetch
git switch main
git pull origin frontend
git pull origin backend

git push origin main

```
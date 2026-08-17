---
name: parabbs-agent
description: ...
---

# ParaBBS Agent

## Workflow

As you wants to work in user's private folders and blogs:
- 1. Use folder api to get user's folder tree.
- 2. Query blogs under a folder.
- 3. Use blog api to Get blog detail by blogId, and then Save content Locally.
- 4. Edit or Create blogs locally until done.
- 5. Update or Create blog online.

## Request

You can send HTTP/HTTPS request to ParaBBS or its based BBS website or local server.

Attention, the request HTTP requires `Authorization: Bearer ${pa_token}`.

## Setup

Check `PARA_AGENT_TOKEN` in `./.env`, if not, ask user this TOKEN.

Token is created on website (address is `PARA_BASE_URL`).

## API

Folder API read `./references/folder.md`

Blog API read `./references/blog.md`
# Faramita BBS Frontend Notes

This project is entering v0.3.0 frontend development. The backend is considered mostly fixed.

## Scope Rules

- Write operations are limited to `Frontend/`.
- Do not patch `Backend/` except when explicitly asked to switch backend configuration to a development profile or start the backend service.
- Treat unrelated working-tree changes as user-owned. Do not revert or overwrite them.

## Development Phases

### Phase 1: Infrastructure Only

Allowed work:

- API client modules and request/response conventions.
- TypeScript domain types and DTO/VO contracts.
- Router records, route names, route meta, and auth guards.
- Store contracts for auth/session/domain state.
- Environment variables, proxy settings, build scripts, and test scaffolding.
- Documentation of frontend conventions.

Disallowed work:

- Page redesigns or new visual compositions.
- Layout changes.
- Style changes, theme changes, animations, or component visual tuning.
- User-facing copy changes unless required by route metadata or infrastructure correctness.

### Phase 2: Page Development

Only start after Phase 1 is explicitly accepted. Focus on page implementation and user workflows.

## Current Frontend Stack

- Vue 3 with `<script setup>` support.
- TypeScript strict mode via `vue-tsc`.
- Vite.
- Vue Router.
- Pinia.
- Axios.
- Naive UI.
- Sass.
- Vditor.

## Commands

Run from `Frontend/faramita-bbs-frontend`:

- `npm run dev`
- `npm run build`
- `npm run preview`

## v0.3.0 Backend Contract Notes

- API base path in development is currently `VITE_API_BASE_URL=/bbs/api`.
- v0.3.0 docs describe unified responses as:
  - `code: 200`
  - `message: string`
  - `data: unknown`
- Existing frontend request code still checks `res.code !== 1`; this should be aligned during Phase 1.
- v0.3.0 replaces old `bloguid` usage with Snowflake `id` values.
- Major frontend contract areas are auth, user profile, public/private blogs, folders, tags, and likes.

## Route Targets From v0.3.0 PRD

- `/`
- `/blog`
- `/blog/:id` for public blog detail.
- `/workspace` for authenticated workspace.
- `/workspace/blogs/:id` for authenticated private blog editing.
- `/login`
- `/register`
- user profile route to be kept compatible with the product decision for public profiles.

## Implementation Bias

- Prefer small, typed modules over page-local API calls.
- Keep route names and API function names stable and explicit.
- Convert form-data requirements through shared helpers where practical.
- Avoid introducing new UI dependencies during Phase 1 unless required for infrastructure tests.
- Keep existing pages visually unchanged during Phase 1.

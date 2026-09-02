FROM node:22-alpine AS build

WORKDIR /app

COPY backend/mcp/package.json ./
COPY backend/mcp/package-lock.json ./

RUN npm ci

COPY backend/mcp/tsconfig.json ./
COPY backend/mcp/src ./src

RUN npm run build


FROM node:22-alpine AS runtime

ENV NODE_ENV=production

WORKDIR /app

COPY backend/mcp/package.json ./
COPY backend/mcp/package-lock.json ./

RUN npm ci --omit=dev \
    && npm cache clean --force

COPY --from=build /app/dist ./dist

USER node

EXPOSE 8081

CMD ["node", "dist/index.js"]
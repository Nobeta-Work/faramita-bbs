import type {
  McpServer
} from "@modelcontextprotocol/server";
import * as z from "zod/v4";

import { ParaBbsClient } from "../client.js";
import { toolResult } from "../tool-result.js";

export function registerBlogTools(
  server: McpServer,
  client: ParaBbsClient
): void {
  server.registerTool(
    "list_folder_blogs",
    {
      description:
        "分页查询当前用户指定目录中的博客。根目录 ID 为 0。",
      inputSchema: z.object({
        folderId: z.string().min(1),
        pageNum: z.number().int().min(1).default(1),
        pageSize: z.number().int().min(1).max(50).default(20)
      })
    },
    async ({ folderId, pageNum, pageSize }) =>
      toolResult(() => {
        const query = new URLSearchParams({
          pageNum: String(pageNum),
          pageSize: String(pageSize)
        });

        return client.get(
          `/api/folders/${encodeURIComponent(folderId)}` +
          `/blogs?${query.toString()}`
        );
      })
  );

  server.registerTool(
    "get_blog",
    {
      description:
        "读取当前用户的一篇博客，包括草稿和未发布内容。",
      inputSchema: z.object({
        blogId: z.string().min(1)
      })
    },
    async ({ blogId }) =>
      toolResult(() =>
        client.get(
          `/api/blogs/me/${encodeURIComponent(blogId)}`
        )
      )
  );

  server.registerTool(
    "create_blog",
    {
      description:
        "在指定目录创建一篇新的博客草稿。",
      inputSchema: z.object({
        title: z.string().min(1).max(50),
        folderId: z.string().min(1).default("0")
      })
    },
    async ({ title, folderId }) =>
      toolResult(() =>
        client.post("/api/blogs/me", {
          title,
          folderId
        })
      )
  );

  server.registerTool(
    "update_blog",
    {
      description:
        "更新当前用户的博客内容、目录、标签和发布状态。",
      inputSchema: z.object({
        blogId: z.string().min(1),
        folderId: z.string().min(1),
        title: z.string().min(1).max(20),
        summary: z.string().max(200).optional(),
        content: z.string().optional(),
        isPublished: z.union([
          z.literal(0),
          z.literal(1)
        ]).optional(),
        tagIds: z.array(z.string().min(1)).optional()
      })
    },
    async ({
      blogId,
      folderId,
      title,
      summary,
      content,
      isPublished,
      tagIds
    }) =>
      toolResult(() =>
        client.put(
          `/api/blogs/me/${encodeURIComponent(blogId)}`,
          {
            folderId,
            title,
            summary,
            content,
            isPublished,
            tagIds
          }
        )
      )
  );
}
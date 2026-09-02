import type {
  McpServer
} from "@modelcontextprotocol/server";
import * as z from "zod/v4";

import { ParaBbsClient } from "../client.js";
import { toolResult } from "../tool-result.js";

export function registerFolderTools(
  server: McpServer,
  client: ParaBbsClient
): void {
  server.registerTool(
    "get_my_workspace",
    {
      description:
        "获取当前 ParaBBS 用户的完整目录树。",
      inputSchema: z.object({})
    },
    async () =>
      toolResult(() =>
        client.get("/api/folders/me")
      )
  );

  server.registerTool(
    "create_folder",
    {
      description:
        "在当前用户的指定父目录下创建目录。",
      inputSchema: z.object({
        parentId: z.string().min(1).default("0"),
        name: z.string().min(1).max(20)
      })
    },
    async ({ parentId, name }) =>
      toolResult(() =>
        client.post("/api/folders", {
          parentId,
          name
        })
      )
  );

  server.registerTool(
    "rename_folder",
    {
      description:
        "重命名当前用户的目录。",
      inputSchema: z.object({
        folderId: z.string().min(1),
        name: z.string().min(1).max(20)
      })
    },
    async ({ folderId, name }) =>
      toolResult(() =>
        client.put(
          `/api/folders/${encodeURIComponent(folderId)}`,
          { name }
        )
      )
  );

  server.registerTool(
    "move_folder",
    {
      description:
        "把当前用户的目录移动到另一个父目录。",
      inputSchema: z.object({
        folderId: z.string().min(1),
        targetParentId: z.string().min(1)
      })
    },
    async ({ folderId, targetParentId }) =>
      toolResult(() =>
        client.put(
          `/api/folders/${encodeURIComponent(folderId)}/move`,
          { targetParentId }
        )
      )
  );

  server.registerTool(
    "move_blogs",
    {
      description:
        "批量移动当前用户的博客到指定目录。",
      inputSchema: z.object({
        blogIds: z.array(z.string().min(1)).min(1),
        targetId: z.string().min(1)
      })
    },
    async ({ blogIds, targetId }) =>
      toolResult(() =>
        client.put("/api/folders/blogs/move", {
          blogIds,
          targetId
        })
      )
  );
}
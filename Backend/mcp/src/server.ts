import { McpServer } from "@modelcontextprotocol/server";
import { ParaBbsClient } from "./client.js";
import { registerBlogTools } from "./tools/blog.js";
import { registerFolderTools } from "./tools/folder.js";

export function buildMcpServer(
    token: string
): McpServer{
    const client = new ParaBbsClient(token);

    const server = new McpServer({
        name: "parabbs",
        version: "0.1.0"
    });

    registerBlogTools(server, client);
    registerFolderTools(server, client);

    return server;
}
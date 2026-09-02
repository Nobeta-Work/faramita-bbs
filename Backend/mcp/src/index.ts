import { createMcpExpressApp, requireBearerAuth } from "@modelcontextprotocol/express";
import { config } from "./config.js";
import { ParaTokenVerifier } from "./auth.js";
import { createMcpHandler } from "@modelcontextprotocol/server";
import { buildMcpServer } from "./server.js";
import { toNodeHandler } from "@modelcontextprotocol/node";

const app = createMcpExpressApp({
    host: config.host,
    allowedHosts: config.allowedHosts
});

app.get("/healthz", (_request, response) => {
    response.status(200).json({
        status: "ok",
        service: "parabbs-mcp"
    });
});

const verifier = new ParaTokenVerifier();

const auth = requireBearerAuth({
    verifier,
    requiredScopes: ["mcp"]
});

const handler = createMcpHandler(
    ({ authInfo}) => {
        if (!authInfo?.token) {
            throw new Error("Missing verified token");
        }

        return buildMcpServer(authInfo.token);
    },
    {
        responseMode: "json"
    }
);

const nodeHandler = toNodeHandler(handler);

app.all(
    "/mcp",
    auth,
    (request, response) => {
        void nodeHandler(
            request,
            response,
            request.body
        );
    }
);

const httpServer = app.listen(
  config.port,
  config.host,
  () => {
    console.log(
      `ParaBBS MCP listening on ` +
      `http://${config.host}:${config.port}/mcp`
    );
  }
);

async function shutdown(): Promise<void> {
  await handler.close();

  httpServer.close(() => {
    process.exit(0);
  });
}

process.on("SIGINT", () => void shutdown());
process.on("SIGTERM", () => void shutdown());
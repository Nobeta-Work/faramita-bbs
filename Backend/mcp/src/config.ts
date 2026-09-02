import process from "node:process";

function parsePositiveInteger(
    value: string | undefined,
    fallback: number
): number {
    const parsed = Number(value ?? fallback);
    if (!Number.isInteger(parsed) || parsed <= 0) {
        return fallback;
    }
    return parsed;
}

export const config = {
    host: process.env.MCP_HOST ?? "0.0.0.0",
    port: parsePositiveInteger(process.env.MCP_PORT, 8081),

    paraBaseUrl: (
        process.env.PARA_BASE_URL ??
        "http://parabbs-backend:8080/bbs"
    ).replace(/\/$/, ""),

    allowedHosts: (
        process.env.MCP_ALLOWED_HOSTS ??
        "localhost,127.0.0.1"
    ).split(",")
        .map(value => value.trim())
        .filter(Boolean)
    ,
    
    requestTimeoutMs: parsePositiveInteger(
        process.env.PARA_REQUEST_TIMEOUT_MS,
        10_000
    ),

    tokenCacheSeconds: parsePositiveInteger(
        process.env.PARA_TOKEN_CACHE_SECONDS,
        30
    )
};
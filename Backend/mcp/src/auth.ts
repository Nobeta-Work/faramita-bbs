import { AuthInfo, OAuthError, OAuthErrorCode, OAuthTokenVerifier } from "@modelcontextprotocol/server";
import { createHash } from "node:crypto";
import { config } from "./config.js";
import { ParaApiError, ParaBbsClient } from "./client.js";

const validTokenCache = new Map<string, number>();

function tokenHash(token: string): string {
    return createHash("sha256")
            .update(token)
            .digest("hex");
}

function invalidToken(): OAuthError {
    return new OAuthError(
        OAuthErrorCode.InvalidToken,
        "Invalid or expired ParaBBS Agent Token"
    );
}

export class ParaTokenVerifier
    implements OAuthTokenVerifier {
    async verifyAccessToken(
        token: string
    ): Promise<AuthInfo> {
        if (!/^pa_[A-Za-z0-9_-]{8,}$/.test(token)) {
            throw invalidToken();
        }

        const hash = tokenHash(token);
        const nowSeconds = Math.floor(Date.now() / 1000);
        const cachedUntil = validTokenCache.get(hash);

        if (!cachedUntil || cachedUntil <= nowSeconds) {
        try {
            await new ParaBbsClient(token).verify();
        } catch (error) {
            if (
            error instanceof ParaApiError &&
            [401, 403, 404].includes(error.code)
            ) {
            throw invalidToken();
            }

            throw error;
        }

        validTokenCache.set(
            hash,
            nowSeconds + config.tokenCacheSeconds
        );
        }

        return {
        token,
        clientId: `parabbs-agent:${hash.slice(0, 16)}`,
        scopes: ["mcp"],
        expiresAt:
            nowSeconds + config.tokenCacheSeconds + 5
        };
    }
}
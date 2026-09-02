import { config } from "./config.js";

interface ParaApiResult<T> {
    code: number,
    msg: string,
    data: T
}

export class ParaApiError extends Error {
    constructor(
        public readonly code: number,
        message: string,
        public readonly httpStatus: number
    ) {
        super(message);
        this.name = "ParaApiError";
    }
}

export class ParaBbsClient {
    constructor(private readonly token: string) {}

    get<T>(path: string): Promise<T> {
        return this.request<T>(path, {
            method: "GET"
        });
    }

    post<T>(path: string, body: unknown): Promise<T> {
        return this.request<T>(path, {
            method: "POST",
            body: JSON.stringify(body)
        })
    }

    put<T>(path: string, body: unknown): Promise<T> {
        return this.request<T>(path, {
        method: "PUT",
        body: JSON.stringify(body)
        });
    }

    verify(): Promise<unknown> {
        return this.get("/api/folders/me");
    }

    private async request<T>(
        path: string,
        init: RequestInit
    ): Promise<T> {
        let response: Response;

        try {
            response = await fetch(
                `${config.paraBaseUrl}${path}`,
                {
                    ...init,
                    headers: {
                        Authorization: `Bearer ${this.token}`,
                        Accept: "application/json",
                        "Content-Type": "application.json",
                        ...init.headers
                    },
                    signal: AbortSignal.timeout(
                        config.requestTimeoutMs
                    )
                }
            );
        } catch {
            throw new ParaApiError(
                503,
                "ParaBBS backend is unavailable",
                503
            );
        }

        let result: ParaApiResult<T>;

        try {
            result = await response.json() as ParaApiResult<T>;
        } catch {
            throw new ParaApiError(
                502,
                "Invalid response from ParaBBS backend",
                response.status
            );
        }

        if (!response.ok || result.code !== 200) {
            throw new ParaApiError(
                result.code ?? response.status,
                result.msg ?? "ParaBBS request failed",
                response.status
            );
        }

        return result.data;
    }
}
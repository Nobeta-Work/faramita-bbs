import { ParaApiError } from "./client.js";

export async function toolResult<T>(
  operation: () => Promise<T>
) {
  try {
    const data = await operation();

    return {
      content: [
        {
          type: "text" as const,
          text: JSON.stringify({
            success: true,
            data
          })
        }
      ]
    };
  } catch (error) {
    if (error instanceof ParaApiError) {
      return {
        isError: true,
        content: [
          {
            type: "text" as const,
            text: JSON.stringify({
              success: false,
              code: error.code,
              message: error.message
            })
          }
        ]
      };
    }

    return {
      isError: true,
      content: [
        {
          type: "text" as const,
          text: JSON.stringify({
            success: false,
            code: 500,
            message: "MCP tool execution failed"
          })
        }
      ]
    };
  }
}
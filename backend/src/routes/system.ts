import { checkDatabase } from "../db/connection";
import { failure, success } from "../lib/http";
import type { Env } from "../types/env";

export function systemInfo(env: Env): Response {
  return success(200, {
    application: "Respondo",
    apiVersion: env.API_VERSION ?? "1.0.0",
    environment: env.ENVIRONMENT ?? "unknown",
    capabilities: ["health", "d1_connectivity"],
  });
}

export async function databaseStatus(env: Env): Promise<Response> {
  try {
    const reachable = await checkDatabase(env);
    if (!reachable) {
      return failure(503, "DATABASE_UNAVAILABLE", "The database is temporarily unavailable.");
    }
    return success(200, {
      status: "connected",
      checkedAt: new Date().toISOString(),
    });
  } catch {
    // Deliberately log no error object, SQL text, request data, or binding details.
    console.error("D1 connectivity check failed");
    return failure(503, "DATABASE_UNAVAILABLE", "The database is temporarily unavailable.");
  }
}

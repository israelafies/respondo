import { success } from "../lib/http";
import type { Env } from "../types/env";

export function health(env: Env): Response {
  return success(200, {
    status: "ok",
    service: "respondo-api",
    timestamp: new Date().toISOString(),
    apiVersion: env.API_VERSION ?? "1.0.0",
  });
}

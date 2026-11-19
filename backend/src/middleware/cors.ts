import { failure } from "../lib/http";
import type { Env } from "../types/env";

const ALLOWED_METHODS = "GET, OPTIONS";
const ALLOWED_HEADERS = "Content-Type, Authorization";

export function isAllowedOrigin(origin: string | null, env: Env): origin is string {
  if (!origin) return false;
  const allowed = (env.CORS_ALLOWED_ORIGINS ?? "")
    .split(",")
    .map((value) => value.trim())
    .filter(Boolean);
  return allowed.includes(origin);
}

export function corsHeaders(origin: string | null, env: Env): Headers {
  const headers = new Headers();
  if (isAllowedOrigin(origin, env)) {
    headers.set("Access-Control-Allow-Origin", origin);
    headers.set("Access-Control-Allow-Methods", ALLOWED_METHODS);
    headers.set("Access-Control-Allow-Headers", ALLOWED_HEADERS);
    headers.set("Access-Control-Max-Age", "600");
  }
  return headers;
}

export function handlePreflight(request: Request, env: Env): Response {
  const origin = request.headers.get("Origin");
  if (!isAllowedOrigin(origin, env)) {
    return failure(403, "ORIGIN_NOT_ALLOWED", "This origin is not allowed.");
  }
  const requestedMethod = request.headers.get("Access-Control-Request-Method");
  if (requestedMethod && requestedMethod !== "GET") {
    return failure(403, "METHOD_NOT_ALLOWED", "This method is not allowed.");
  }
  const requestedHeaders = (request.headers.get("Access-Control-Request-Headers") ?? "")
    .split(",")
    .map((value) => value.trim().toLowerCase())
    .filter(Boolean);
  if (requestedHeaders.some((name) => !["content-type", "authorization"].includes(name))) {
    return failure(403, "HEADERS_NOT_ALLOWED", "One or more request headers are not allowed.");
  }
  return new Response(null, {
    status: 204,
    headers: corsHeaders(origin, env),
  });
}

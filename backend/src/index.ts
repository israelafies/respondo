import { corsHeaders, handlePreflight } from "./middleware/cors";
import { failure } from "./lib/http";
import { health } from "./routes/health";
import { databaseStatus, systemInfo } from "./routes/system";
import type { Env } from "./types/env";

const MAX_REQUEST_BYTES = 16 * 1024;

const routes = new Map<string, (env: Env) => Response | Promise<Response>>([
  ["GET /health", (env) => health(env)],
  ["GET /api/v1/system/info", (env) => systemInfo(env)],
  ["GET /api/v1/system/database", (env) => databaseStatus(env)],
]);

function withCors(response: Response, request: Request, env: Env): Response {
  const headers = new Headers(response.headers);
  for (const [name, value] of corsHeaders(request.headers.get("Origin"), env)) {
    headers.set(name, value);
  }
  headers.set("Cache-Control", "no-store");
  headers.set("X-Content-Type-Options", "nosniff");
  headers.set("X-Frame-Options", "DENY");
  headers.set("Referrer-Policy", "no-referrer");
  if (!headers.has("Vary")) headers.set("Vary", "Origin");
  return new Response(response.body, { status: response.status, statusText: response.statusText, headers });
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    try {
      if (request.method === "OPTIONS") {
        return withCors(handlePreflight(request, env), request, env);
      }

      const length = request.headers.get("Content-Length");
      if (length !== null) {
        if (!/^\d+$/.test(length)) {
          return withCors(failure(400, "INVALID_CONTENT_LENGTH", "The request is invalid."), request, env);
        }
        if (Number(length) > MAX_REQUEST_BYTES) {
          return withCors(failure(413, "REQUEST_TOO_LARGE", "The request exceeds the allowed size."), request, env);
        }
      }

      const url = new URL(request.url);
      const route = routes.get(`${request.method} ${url.pathname}`);
      if (route) return withCors(await route(env), request, env);

      const knownPath = [...routes.keys()].some((key) => key.slice(key.indexOf(" ") + 1) === url.pathname);
      if (knownPath) {
        return withCors(
          failure(405, "METHOD_NOT_ALLOWED", "The requested method is not allowed."),
          request,
          env,
        );
      }

      return withCors(failure(404, "NOT_FOUND", "The requested resource was not found."), request, env);
    } catch {
      // Keep production responses generic and avoid logging request or error contents.
      console.error("Unhandled Worker request failure");
      return withCors(
        failure(500, "INTERNAL_ERROR", "The request could not be completed."),
        request,
        env,
      );
    }
  },
};

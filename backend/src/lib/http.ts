export interface ApiError {
  code: string;
  message: string;
}

export interface ApiEnvelope<T> {
  success: boolean;
  data: T | null;
  error: ApiError | null;
}

export function jsonResponse<T>(
  status: number,
  body: ApiEnvelope<T>,
  extraHeaders?: HeadersInit,
): Response {
  const headers = new Headers(extraHeaders);
  headers.set("Content-Type", "application/json; charset=utf-8");
  headers.set("Cache-Control", "no-store");
  headers.set("X-Content-Type-Options", "nosniff");
  headers.set("X-Frame-Options", "DENY");
  headers.set("Referrer-Policy", "no-referrer");
  headers.append("Vary", "Origin");

  return new Response(JSON.stringify(body), { status, headers });
}

export function success<T>(status: number, data: T, headers?: HeadersInit): Response {
  return jsonResponse(status, { success: true, data, error: null }, headers);
}

export function failure(
  status: number,
  code: string,
  message: string,
  headers?: HeadersInit,
): Response {
  return jsonResponse(status, {
    success: false,
    data: null,
    error: { code, message },
  }, headers);
}

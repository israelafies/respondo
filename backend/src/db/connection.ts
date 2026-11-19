import type { Env } from "../types/env";

export async function checkDatabase(env: Env): Promise<boolean> {
  const result = await env.DB.prepare("SELECT 1 AS reachable").first<{ reachable: number }>();
  return result?.reachable === 1;
}

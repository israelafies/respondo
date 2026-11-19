import { applyD1Migrations } from "cloudflare:test";
import { env as workerEnv } from "cloudflare:workers";
import { beforeAll, describe, expect, it } from "vitest";
import type { D1Migration } from "@cloudflare/vitest-plugin";
import worker from "../src/index";
import type { Env } from "../src/types/env";

const testBindings = workerEnv as unknown as Env & { TEST_MIGRATIONS: D1Migration[] };
const testEnv = testBindings as Env;

beforeAll(async () => {
  await applyD1Migrations(testEnv.DB, testBindings.TEST_MIGRATIONS);
});

async function request(path: string, init?: RequestInit, env = testEnv): Promise<Response> {
  return worker.fetch(new Request(`https://api.example.test${path}`, init), env);
}

describe("Worker API foundation", () => {
  it("applies the initial migration and creates all expected tables and indexes", async () => {
    const tables = await testEnv.DB
      .prepare("SELECT name FROM sqlite_master WHERE type = 'table'")
      .all<{ name: string }>();
    const indexes = await testEnv.DB
      .prepare("SELECT name FROM sqlite_master WHERE type = 'index'")
      .all<{ name: string }>();
    const foreignKeyCheck = await testEnv.DB.prepare("PRAGMA foreign_key_check").all();

    expect(tables.results.map(({ name }) => name)).toEqual(expect.arrayContaining([
      "users",
      "user_settings",
      "prospects",
      "conversations",
      "messages",
      "ai_provider_settings",
      "activity_logs",
    ]));
    expect(indexes.results.map(({ name }) => name)).toEqual(expect.arrayContaining([
      "idx_prospects_user_created",
      "idx_conversations_user_updated",
      "idx_conversations_prospect",
      "idx_messages_conversation_created",
      "idx_ai_provider_settings_route",
      "idx_activity_logs_user_created",
    ]));
    expect(foreignKeyCheck.results).toHaveLength(0);
  });

  it("returns the health check using the standard JSON envelope", async () => {
    const response = await request("/health");
    const body = await response.json() as Record<string, unknown>;
    const data = body.data as Record<string, unknown>;

    expect(response.status).toBe(200);
    expect(body).toMatchObject({ success: true, error: null });
    expect(data).toMatchObject({ status: "ok", service: "respondo-api", apiVersion: "1.0.0" });
    expect(typeof data.timestamp).toBe("string");
    expect(response.headers.get("X-Content-Type-Options")).toBe("nosniff");
  });

  it("returns safe system information", async () => {
    const response = await request("/api/v1/system/info");
    const body = await response.json() as Record<string, unknown>;

    expect(response.status).toBe(200);
    expect(body).toMatchObject({ success: true, error: null });
    expect(body.data).toMatchObject({ application: "Respondo", environment: "test", apiVersion: "1.0.0" });
    expect(JSON.stringify(body)).not.toContain("D1_DATABASE_ID");
  });

  it("returns a safe 404 envelope for unknown routes", async () => {
    const response = await request("/api/v1/not-a-route");
    expect(response.status).toBe(404);
    await expect(response.json()).resolves.toMatchObject({
      success: false,
      data: null,
      error: { code: "NOT_FOUND" },
    });
  });

  it("handles an allowed browser preflight", async () => {
    const response = await request("/api/v1/system/info", {
      method: "OPTIONS",
      headers: { Origin: "https://app.example.test", "Access-Control-Request-Method": "GET" },
    });
    expect(response.status).toBe(204);
    expect(response.headers.get("Access-Control-Allow-Origin")).toBe("https://app.example.test");
  });

  it("rejects preflight from an unconfigured origin", async () => {
    const response = await request("/api/v1/system/info", {
      method: "OPTIONS",
      headers: { Origin: "https://evil.example", "Access-Control-Request-Method": "GET" },
    });
    expect(response.status).toBe(403);
    expect(response.headers.get("Access-Control-Allow-Origin")).toBeNull();
  });

  it("rejects a preflight method outside the supported API methods", async () => {
    const response = await request("/api/v1/system/info", {
      method: "OPTIONS",
      headers: { Origin: "https://app.example.test", "Access-Control-Request-Method": "POST" },
    });
    expect(response.status).toBe(403);
    await expect(response.json()).resolves.toMatchObject({
      success: false,
      error: { code: "METHOD_NOT_ALLOWED" },
    });
  });

  it("does not grant CORS access to an unconfigured origin", async () => {
    const response = await request("/health", { headers: { Origin: "https://evil.example" } });
    expect(response.status).toBe(200);
    expect(response.headers.get("Access-Control-Allow-Origin")).toBeNull();
  });

  it("returns database connectivity without exposing D1 details", async () => {
    const response = await request("/api/v1/system/database");
    const body = await response.json() as Record<string, unknown>;
    expect(response.status).toBe(200);
    expect(body.data).toMatchObject({ status: "connected" });
    expect(typeof (body.data as Record<string, unknown>).checkedAt).toBe("string");
  });

  it("rejects orphaned and cross-user prospect/conversation/message relationships", async () => {
    await testEnv.DB.prepare("INSERT INTO users (id, email) VALUES (?, ?)")
      .bind("fk-user-a", "fk-a@example.test")
      .run();
    await testEnv.DB.prepare("INSERT INTO users (id, email) VALUES (?, ?)")
      .bind("fk-user-b", "fk-b@example.test")
      .run();
    await testEnv.DB.prepare("INSERT INTO prospects (id, user_id, full_name) VALUES (?, ?, ?)")
      .bind("fk-prospect-b", "fk-user-b", "Prospect B")
      .run();

    await expect(testEnv.DB.prepare("INSERT INTO user_settings (user_id) VALUES (?)")
      .bind("missing-user")
      .run()).rejects.toThrow();
    await expect(testEnv.DB.prepare("INSERT INTO conversations (id, user_id, prospect_id) VALUES (?, ?, ?)")
      .bind("fk-cross-user-conversation", "fk-user-a", "fk-prospect-b")
      .run()).rejects.toThrow();

    await testEnv.DB.prepare("INSERT INTO conversations (id, user_id) VALUES (?, ?)")
      .bind("fk-conversation-a", "fk-user-a")
      .run();
    await expect(testEnv.DB.prepare(
      "INSERT INTO messages (id, user_id, conversation_id, role, content) VALUES (?, ?, ?, ?, ?)",
    ).bind("fk-cross-user-message", "fk-user-b", "fk-conversation-a", "user", "test")
      .run()).rejects.toThrow();
  });

  it("enforces prospect deletion restrictions and cascades owned data", async () => {
    const userId = "delete-user";
    await testEnv.DB.prepare("INSERT INTO users (id, email) VALUES (?, ?)")
      .bind(userId, "delete@example.test")
      .run();
    await testEnv.DB.prepare("INSERT INTO user_settings (user_id) VALUES (?)").bind(userId).run();
    await testEnv.DB.prepare("INSERT INTO prospects (id, user_id, full_name) VALUES (?, ?, ?)")
      .bind("delete-prospect", userId, "Delete Prospect")
      .run();
    await testEnv.DB.prepare("INSERT INTO conversations (id, user_id, prospect_id) VALUES (?, ?, ?)")
      .bind("delete-conversation", userId, "delete-prospect")
      .run();
    await testEnv.DB.prepare(
      "INSERT INTO messages (id, user_id, conversation_id, role, content) VALUES (?, ?, ?, ?, ?)",
    ).bind("delete-message", userId, "delete-conversation", "user", "test").run();
    await testEnv.DB.prepare(
      "INSERT INTO ai_provider_settings (user_id, provider) VALUES (?, ?)",
    ).bind(userId, "groq").run();
    await testEnv.DB.prepare(
      "INSERT INTO activity_logs (id, user_id, action) VALUES (?, ?, ?)",
    ).bind("delete-log", userId, "test").run();

    await expect(testEnv.DB.prepare("DELETE FROM prospects WHERE id = ?")
      .bind("delete-prospect")
      .run()).rejects.toThrow();

    await testEnv.DB.prepare("DELETE FROM conversations WHERE id = ?")
      .bind("delete-conversation")
      .run();
    const messageCount = await testEnv.DB.prepare("SELECT COUNT(*) AS count FROM messages WHERE id = ?")
      .bind("delete-message")
      .first<{ count: number }>();
    expect(messageCount?.count).toBe(0);

    await testEnv.DB.prepare("DELETE FROM prospects WHERE id = ?").bind("delete-prospect").run();
    await testEnv.DB.prepare("DELETE FROM users WHERE id = ?").bind(userId).run();

    for (const table of ["user_settings", "prospects", "conversations", "messages", "ai_provider_settings", "activity_logs"]) {
      const row = await testEnv.DB.prepare(`SELECT COUNT(*) AS count FROM ${table} WHERE user_id = ?`)
        .bind(userId)
        .first<{ count: number }>();
      expect(row?.count).toBe(0);
    }
  });

  it("maps D1 failures to a safe service-unavailable response", async () => {
    const brokenEnv = {
      ...testEnv,
      DB: { prepare: () => { throw new Error("sensitive SQL and account detail"); } },
    } as unknown as Env;
    const response = await request("/api/v1/system/database", undefined, brokenEnv);
    const text = await response.text();
    expect(response.status).toBe(503);
    expect(text).toContain("DATABASE_UNAVAILABLE");
    expect(text).not.toContain("sensitive SQL");
  });

  it("rejects methods that do not match a known endpoint", async () => {
    const response = await request("/api/v1/system/info", { method: "POST" });
    expect(response.status).toBe(405);
    await expect(response.json()).resolves.toMatchObject({
      success: false,
      error: { code: "METHOD_NOT_ALLOWED" },
    });
  });

  it("rejects requests with an invalid content length", async () => {
    const response = await request("/health", { headers: { "Content-Length": "invalid" } });
    expect(response.status).toBe(400);
  });

  it("rejects requests larger than the configured limit", async () => {
    const response = await request("/health", { headers: { "Content-Length": "20000" } });
    expect(response.status).toBe(413);
  });

  it("does not expose user data routes before authentication exists", async () => {
    const response = await request("/api/v1/prospects", { method: "POST" });
    expect(response.status).toBe(404);
  });
});

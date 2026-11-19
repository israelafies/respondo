import { spawnSync } from "node:child_process";

const wrangler = "./node_modules/.bin/wrangler";

function execute(sql, label) {
  const result = spawnSync(wrangler, [
    "d1",
    "execute",
    "respondo-db",
    "--local",
    "--command",
    sql,
  ], { encoding: "utf8" });

  if (result.error) throw result.error;
  if (result.status !== 0) {
    throw new Error(`${label} failed:\n${result.stdout}${result.stderr}`);
  }
  return `${result.stdout}${result.stderr}`;
}

function expectForeignKeyFailure(sql, label) {
  const output = executeResult(sql);
  if (output.status === 0) throw new Error(`${label} unexpectedly succeeded.`);
  const message = `${output.stdout}${output.stderr}`;
  if (!message.includes("FOREIGN KEY constraint failed")) {
    throw new Error(`${label} failed for an unexpected reason:\n${message}`);
  }
}

function executeResult(sql) {
  return spawnSync(wrangler, [
    "d1",
    "execute",
    "respondo-db",
    "--local",
    "--command",
    sql,
  ], { encoding: "utf8" });
}

execute(
  "DELETE FROM users WHERE id IN ('verify-owner-a', 'verify-owner-b', 'verify-cascade-user')",
  "Previous local verifier fixture cleanup",
);

const tableChecks = [
  "SELECT id FROM users LIMIT 0",
  "SELECT user_id FROM user_settings LIMIT 0",
  "SELECT id FROM prospects LIMIT 0",
  "SELECT id FROM conversations LIMIT 0",
  "SELECT id FROM messages LIMIT 0",
  "SELECT user_id FROM ai_provider_settings LIMIT 0",
  "SELECT id FROM activity_logs LIMIT 0",
];
execute(tableChecks.join(";"), "Expected D1 tables");

const indexChecks = [
  "EXPLAIN QUERY PLAN SELECT id FROM prospects INDEXED BY idx_prospects_user_created WHERE user_id = 'verify'",
  "EXPLAIN QUERY PLAN SELECT id FROM conversations INDEXED BY idx_conversations_user_updated WHERE user_id = 'verify'",
  "EXPLAIN QUERY PLAN SELECT id FROM conversations INDEXED BY idx_conversations_prospect WHERE prospect_id = 'verify'",
  "EXPLAIN QUERY PLAN SELECT id FROM messages INDEXED BY idx_messages_conversation_created WHERE user_id = 'verify' AND conversation_id = 'verify' ORDER BY created_at",
  "EXPLAIN QUERY PLAN SELECT user_id FROM ai_provider_settings INDEXED BY idx_ai_provider_settings_route WHERE user_id = 'verify' AND enabled = 1 ORDER BY priority",
  "EXPLAIN QUERY PLAN SELECT id FROM activity_logs INDEXED BY idx_activity_logs_user_created WHERE user_id = 'verify' ORDER BY created_at DESC",
];
execute(indexChecks.join(";"), "Expected D1 indexes");

execute([
  "INSERT INTO users (id, email) VALUES ('verify-owner-a', 'verify-a@example.test')",
  "INSERT INTO users (id, email) VALUES ('verify-owner-b', 'verify-b@example.test')",
  "INSERT INTO prospects (id, user_id, full_name) VALUES ('verify-prospect-a', 'verify-owner-a', 'A')",
  "INSERT INTO prospects (id, user_id, full_name) VALUES ('verify-prospect-b', 'verify-owner-b', 'B')",
  "INSERT INTO conversations (id, user_id, prospect_id) VALUES ('verify-conversation-a', 'verify-owner-a', 'verify-prospect-a')",
  "INSERT INTO messages (id, user_id, conversation_id, role, content) VALUES ('verify-message-a', 'verify-owner-a', 'verify-conversation-a', 'user', 'test')",
].join(";"), "D1 relationship fixtures");

expectForeignKeyFailure(
  "INSERT INTO user_settings (user_id) VALUES ('verify-missing-user')",
  "Orphaned user_settings reference",
);
expectForeignKeyFailure(
  "INSERT INTO conversations (id, user_id, prospect_id) VALUES ('verify-cross-conversation', 'verify-owner-a', 'verify-prospect-b')",
  "Cross-user prospect reference",
);
expectForeignKeyFailure(
  "INSERT INTO messages (id, user_id, conversation_id, role, content) VALUES ('verify-cross-message', 'verify-owner-b', 'verify-conversation-a', 'user', 'test')",
  "Cross-user conversation reference",
);
expectForeignKeyFailure(
  "DELETE FROM prospects WHERE id = 'verify-prospect-a'",
  "Deletion of a prospect referenced by conversation history",
);

execute([
  "DELETE FROM conversations WHERE id = 'verify-conversation-a'",
  "INSERT INTO conversations (id, user_id) VALUES ('verify-conversation-b', 'verify-owner-a')",
  "INSERT INTO messages (id, user_id, conversation_id, role, content) VALUES ('verify-message-a', 'verify-owner-a', 'verify-conversation-b', 'user', 'cascade verified')",
  "INSERT INTO users (id, email) VALUES ('verify-cascade-user', 'verify-cascade@example.test')",
  "INSERT INTO user_settings (user_id) VALUES ('verify-cascade-user')",
  "INSERT INTO prospects (id, user_id, full_name) VALUES ('verify-cascade-prospect', 'verify-cascade-user', 'Cascade')",
  "INSERT INTO conversations (id, user_id, prospect_id) VALUES ('verify-cascade-conversation', 'verify-cascade-user', 'verify-cascade-prospect')",
  "INSERT INTO messages (id, user_id, conversation_id, role, content) VALUES ('verify-cascade-message', 'verify-cascade-user', 'verify-cascade-conversation', 'user', 'cascade')",
  "INSERT INTO ai_provider_settings (user_id, provider) VALUES ('verify-cascade-user', 'groq')",
  "INSERT INTO activity_logs (id, user_id, action) VALUES ('verify-cascade-log', 'verify-cascade-user', 'verify')",
  "DELETE FROM users WHERE id = 'verify-cascade-user'",
  "INSERT INTO users (id, email) VALUES ('verify-cascade-user', 'verify-cascade@example.test')",
  "INSERT INTO user_settings (user_id) VALUES ('verify-cascade-user')",
  "INSERT INTO prospects (id, user_id, full_name) VALUES ('verify-cascade-prospect', 'verify-cascade-user', 'Cascade')",
  "INSERT INTO conversations (id, user_id, prospect_id) VALUES ('verify-cascade-conversation', 'verify-cascade-user', 'verify-cascade-prospect')",
  "INSERT INTO messages (id, user_id, conversation_id, role, content) VALUES ('verify-cascade-message', 'verify-cascade-user', 'verify-cascade-conversation', 'user', 'cascade')",
  "INSERT INTO ai_provider_settings (user_id, provider) VALUES ('verify-cascade-user', 'groq')",
  "INSERT INTO activity_logs (id, user_id, action) VALUES ('verify-cascade-log', 'verify-cascade-user', 'verify')",
].join(";"), "D1 deletion and cascade behavior");

execute(
  "DELETE FROM users WHERE id IN ('verify-owner-a', 'verify-owner-b', 'verify-cascade-user')",
  "Local verifier fixture cleanup",
);

console.log("Wrangler-local D1 schema, indexes, foreign keys, and deletion behavior verified.");

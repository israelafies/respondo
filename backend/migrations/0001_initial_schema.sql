-- Cloud records are owned by a server-side account. This migration intentionally
-- contains no password field: authentication has not been implemented yet.

CREATE TABLE users (
  id TEXT PRIMARY KEY NOT NULL,
  email TEXT NOT NULL COLLATE NOCASE UNIQUE,
  display_name TEXT,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE TABLE user_settings (
  user_id TEXT PRIMARY KEY NOT NULL,
  provider_selection_mode TEXT NOT NULL DEFAULT 'manual'
    CHECK (provider_selection_mode IN ('manual', 'automatic')),
  selected_provider TEXT
    CHECK (selected_provider IS NULL OR selected_provider IN ('groq', 'gemini', 'openai', 'deepseek')),
  response_tone TEXT NOT NULL DEFAULT 'friendly'
    CHECK (response_tone IN ('friendly', 'professional', 'persuasive', 'casual', 'direct')),
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE prospects (
  id TEXT PRIMARY KEY NOT NULL,
  user_id TEXT NOT NULL,
  full_name TEXT NOT NULL,
  business_name TEXT,
  phone TEXT,
  business_description TEXT,
  problem_or_weakness TEXT,
  how_can_help TEXT,
  service_or_offer TEXT,
  additional_notes TEXT,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  UNIQUE (user_id, id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_prospects_user_created ON prospects(user_id, created_at DESC);

CREATE TABLE conversations (
  id TEXT PRIMARY KEY NOT NULL,
  user_id TEXT NOT NULL,
  prospect_id TEXT,
  title TEXT,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  UNIQUE (user_id, id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id, prospect_id) REFERENCES prospects(user_id, id) ON DELETE NO ACTION
);

CREATE INDEX idx_conversations_user_updated ON conversations(user_id, updated_at DESC);
CREATE INDEX idx_conversations_prospect ON conversations(prospect_id);

CREATE TABLE messages (
  id TEXT PRIMARY KEY NOT NULL,
  user_id TEXT NOT NULL,
  conversation_id TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('user', 'assistant', 'system')),
  content TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  FOREIGN KEY (user_id, conversation_id) REFERENCES conversations(user_id, id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_conversation_created ON messages(user_id, conversation_id, created_at);

CREATE TABLE ai_provider_settings (
  user_id TEXT NOT NULL,
  provider TEXT NOT NULL CHECK (provider IN ('groq', 'gemini', 'openai', 'deepseek')),
  enabled INTEGER NOT NULL DEFAULT 0 CHECK (enabled IN (0, 1)),
  priority INTEGER NOT NULL DEFAULT 100 CHECK (priority > 0),
  model_id TEXT,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  PRIMARY KEY (user_id, provider),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_ai_provider_settings_route ON ai_provider_settings(user_id, enabled, priority);

CREATE TABLE activity_logs (
  id TEXT PRIMARY KEY NOT NULL,
  user_id TEXT NOT NULL,
  action TEXT NOT NULL,
  entity_type TEXT,
  entity_id TEXT,
  metadata_json TEXT CHECK (metadata_json IS NULL OR json_valid(metadata_json)),
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_activity_logs_user_created ON activity_logs(user_id, created_at DESC);

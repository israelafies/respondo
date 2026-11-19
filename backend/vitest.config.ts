import { fileURLToPath } from "node:url";
import { cloudflareTest, readD1Migrations } from "@cloudflare/vitest-plugin";
import { defineConfig } from "vitest/config";

const migrations = await readD1Migrations(fileURLToPath(new URL("./migrations", import.meta.url)));

export default defineConfig({
  plugins: [
    cloudflareTest({
      wrangler: { configPath: "./wrangler.toml" },
      miniflare: {
        bindings: {
          ENVIRONMENT: "test",
          API_VERSION: "1.0.0",
          CORS_ALLOWED_ORIGINS: "https://app.example.test",
          TEST_MIGRATIONS: migrations,
        },
      },
    }),
  ],
  test: {
    include: ["tests/**/*.test.ts"],
    testTimeout: 10_000,
  },
});

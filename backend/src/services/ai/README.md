# Future AI service boundary

This directory is reserved for server-side AI service contracts and provider adapters in a later milestone. No provider is implemented or reported operational by this foundation.

When AI work begins:

- Define one provider-independent request/response contract here, including actual provider/model, provider-reported usage, and structured failures.
- Keep provider-specific HTTP and response mapping in isolated adapters so selection and routing remain backend concerns.
- Read credentials only from Cloudflare Worker secret bindings such as `env.GROQ_API_KEY`; configure them with `wrangler secret put GROQ_API_KEY --env production` after an authorized Cloudflare account is available.
- Keep per-user model and routing preferences in D1 only after authenticated, owner-scoped APIs exist. Never put secret values in `ai_provider_settings`, ordinary Wrangler variables, logs, or Android.
- Bound request context and timeouts, avoid logging prompts/messages, and do not claim generation or usage when the provider did not return it.

Provider credentials, generation requests, routing, and AI history are not implemented in this milestone.

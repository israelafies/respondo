# Respondo project rules

## Product direction

Respondo is an Android-first AI sales automation app. Build it as a native Android application with Kotlin and Jetpack Compose. Keep the architecture modular and provider-independent so AI providers and messaging integrations can change without rewriting prospect, conversation, or approval logic.

The product is intended to manage prospects and business context, draft personalized sales conversations, preserve conversation history, and support human-approved or explicitly controlled automated replies. It may connect to Groq, Google Gemini, OpenAI, and DeepSeek through provider adapters. It may observe supported WhatsApp Messenger and WhatsApp Business notifications through Android's `NotificationListenerService`. Evaluate `AccessibilityService` only for permitted, user-visible messaging interactions and only after validating platform policy and technical constraints. Do not make the WhatsApp Business API a required dependency.

## Architecture rules

- Keep domain models and use cases independent of Android UI, provider SDKs, and messaging apps.
- Put provider-specific request/response mapping behind a common AI provider interface. Keep credentials out of source control and avoid logging prompts, tokens, or message content by default.
- Put notification and any permitted accessibility integration behind messaging-channel interfaces. Treat OS and third-party app behavior as external, unreliable inputs; do not assume private WhatsApp APIs or undocumented contracts.
- Keep conversation history, sales context, and approval state under Respondo's control. Make approval and automation state explicit; do not silently send messages.
- Request Android permissions only when the related feature is introduced, explain why they are needed, and handle denial or revoked access gracefully.
- Keep UI state and presentation in Compose-facing layers; keep business rules in domain/use-case code and persistence behind repository interfaces.
- Add dependencies only when a phase needs them. Prefer AndroidX and Kotlin/Compose platform facilities; avoid premature SDKs and framework abstractions.
- Keep modules small and independently understandable. Avoid circular dependencies. The app module is the composition root.

## Proposed project layout

This is the intended direction, not a mandate to create every module up front:

```text
app/                         # Android entry point, navigation, dependency wiring
core/
  model/                     # Shared domain/value models
  common/                    # Small cross-cutting primitives only when needed
  data/                      # Repository implementations and persistence boundary
feature/
  prospects/                 # Prospect list and business profile flows
  conversations/             # Conversation history and sales context
  approvals/                 # Draft review and controlled reply decisions
  settings/                  # Provider and integration configuration
integration/
  ai-api/                    # Provider-independent AI contracts
  ai-groq/                   # Groq adapter, introduced in its implementation phase
  ai-gemini/                 # Gemini adapter, introduced in its implementation phase
  ai-openai/                 # OpenAI adapter, introduced in its implementation phase
  ai-deepseek/               # DeepSeek adapter, introduced in its implementation phase
  messaging-api/              # Provider-independent inbound/outbound channel contracts
  messaging-notifications/   # NotificationListenerService adapter
  messaging-accessibility/   # Optional, policy-reviewed AccessibilityService adapter
``` 

Do not create empty modules just to match this diagram. Start with the smallest useful app and domain boundaries, then extract modules when ownership or build-time isolation benefits justify it. Keep provider modules optional so the app does not depend on every provider.

## Delivery practices

- Work in small phases with a clear user-visible outcome and focused verification.
- Before adding platform integrations, confirm required permissions, user disclosures, failure behavior, and applicable Android/store policies.
- Keep changes reviewable; do not scaffold the entire roadmap in one change.
- Record meaningful architectural decisions in documentation when they affect module boundaries, data handling, or platform access.
- Do not claim a build or test passed unless it was actually run in an environment with the required Android tooling.

## Current repository state

The initial repository contains no Android project or build configuration. Java 17 is available in the inspected environment; Gradle, an Android SDK, `adb`, and `kotlinc` were not found. See `README.md` for setup prerequisites and the staged starting plan.

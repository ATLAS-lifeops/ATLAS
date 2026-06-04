# LLM Setup

v0.6.0 adds an optional LLM abstraction layer for ATLAS flows: day plans, weekly reports and structured questions inside the product scope.

LLM is optional. ATLAS continues to work without LLM, without an API key and with incomplete LLM configuration. In those cases ATLAS uses deterministic responses.

## Supported Provider Type

The supported provider type is an OpenAI-compatible HTTP API:

```text
POST {ATLAS_LLM_BASE_URL}/chat/completions
```

Compatible endpoints can include local OpenAI-compatible services, OpenRouter-compatible endpoints, Groq-compatible endpoints and other gateways if they support the same chat completions format.

Gemini, Groq, OpenRouter and Hugging Face are reserved as separate provider types for later releases. In v0.6.0 they can be used only through an OpenAI-compatible endpoint when the provider offers one.

## Environment Variables

```bash
ATLAS_LLM_ENABLED=false
ATLAS_LLM_PROVIDER=openai_compatible
ATLAS_LLM_BASE_URL=
ATLAS_LLM_API_KEY=
ATLAS_LLM_MODEL=
ATLAS_LLM_TIMEOUT_SECONDS=20
ATLAS_LLM_MAX_OUTPUT_TOKENS=700
ATLAS_LLM_TEMPERATURE=0.3
ATLAS_LLM_CONNECT_TIMEOUT_SECONDS=5
ATLAS_LLM_RETRY_ENABLED=true
ATLAS_LLM_MAX_RETRIES=2
ATLAS_LLM_DAY_PLAN_ENABLED=true
ATLAS_LLM_REPORT_ENABLED=true
ATLAS_LLM_QUESTION_ENABLED=true
```

Example `.env`:

```bash
ATLAS_LLM_ENABLED=true
ATLAS_LLM_PROVIDER=openai_compatible
ATLAS_LLM_BASE_URL=
ATLAS_LLM_API_KEY=
ATLAS_LLM_MODEL=
```

Do not commit `.env` files or API keys.

## Disable LLM

```bash
ATLAS_LLM_ENABLED=false
```

You can also disable individual flows:

```bash
ATLAS_LLM_DAY_PLAN_ENABLED=false
ATLAS_LLM_REPORT_ENABLED=false
ATLAS_LLM_QUESTION_ENABLED=false
```

## Safety And Privacy

ATLAS is not a doctor, therapist, dietitian or medical specialist. LLM prompts prohibit diagnosis, treatment instructions and unsafe advice. For serious symptoms, ATLAS uses safe fallback responses and recommends contacting a qualified professional.

Do not send sensitive data to free-tier or untrusted providers. Provider free tiers may change. Always check current limits on official provider pages.

## Troubleshooting

- `401/403`: check the API key, model and provider access.
- `429`: provider rate limit reached; ATLAS falls back to deterministic behavior.
- `timeout`: increase timeout or check endpoint availability.
- `invalid model`: check `ATLAS_LLM_MODEL`.
- `fallback mode`: if LLM is disabled, incomplete or unavailable, ATLAS keeps working in deterministic mode.

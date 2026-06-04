# LLM setup

v0.6.0 добавляет опциональный LLM abstraction layer для сценариев ATLAS: план дня, недельный отчёт и структурированные вопросы в рамках продукта.

LLM не обязателен. ATLAS продолжает работать без LLM, без API key и с неполной LLM-конфигурацией. В таких случаях используются deterministic responses.

## Поддерживаемый тип провайдера

Сейчас поддерживается OpenAI-compatible HTTP API:

```text
POST {ATLAS_LLM_BASE_URL}/chat/completions
```

Через compatible endpoints можно подключать локальные OpenAI-compatible endpoints, OpenRouter-compatible endpoint, Groq-compatible endpoint и другие gateways, если они поддерживают тот же формат chat completions.

Gemini, Groq, OpenRouter и Hugging Face зарезервированы как отдельные provider types на будущее. В v0.6.0 они используются только через OpenAI-compatible endpoint, если выбранный сервис его предоставляет.

## Environment variables

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

Не добавляй `.env` и API keys в git.

## Как отключить LLM

```bash
ATLAS_LLM_ENABLED=false
```

Также можно отключить отдельные сценарии:

```bash
ATLAS_LLM_DAY_PLAN_ENABLED=false
ATLAS_LLM_REPORT_ENABLED=false
ATLAS_LLM_QUESTION_ENABLED=false
```

## Safety и privacy

ATLAS не является врачом, терапевтом, диетологом или медицинским специалистом. LLM prompts запрещают диагнозы, лечебные инструкции и unsafe advice. При серьёзных симптомах ATLAS использует безопасный fallback и рекомендует обратиться к квалифицированному специалисту.

Не отправляй чувствительные данные бесплатным или непроверенным провайдерам. Бесплатные тарифы провайдеров могут меняться. Проверяйте актуальные лимиты на официальных страницах провайдеров.

## Troubleshooting

- `401/403`: проверь API key, модель и права доступа.
- `429`: достигнут rate limit провайдера; ATLAS вернётся к deterministic fallback.
- `timeout`: увеличь timeout или проверь доступность endpoint.
- `invalid model`: проверь значение `ATLAS_LLM_MODEL`.
- `fallback mode`: если LLM отключён, неполно настроен или провайдер недоступен, ATLAS продолжит работать deterministic mode.

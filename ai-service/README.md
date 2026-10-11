# Bhumi Vikas AI Service (Python)

This is a Python-only internal service that isolates LLM/provider concerns from the Java backend. It uses only the Python standard library at runtime, so no framework or package installation is required.

## Ownership boundary

- `ai-service/`: provider credentials, prompt/schema, structured provider calls and Hindi/English intent proposals.
- `backend/`: public API, rule corpus, deterministic retrieval, unit validation, calculations, citations and safe fallback behavior.
- `frontend/`: calls only the Java backend.

The AI service does not decide applicable law, choose legal table rows, calculate fees/setbacks or create authoritative citations. The Java backend treats every response as an untrusted proposal and independently validates it.

## Structure

```text
ai_service/
├── app.py                  standard-library internal HTTP server
├── config.py               environment configuration
├── models.py               backend-compatible response model
├── providers/
│   ├── base.py             provider protocol and safe exception
│   └── anthropic.py        provider-specific HTTP adapter
└── services/
    └── query_intent.py     prompt, schema and measurement validation
tests/                      unit and HTTP contract tests
```

## Run locally

From `ai-service/`:

```bash
export ANTHROPIC_API_KEY="..."
export ANTHROPIC_MODEL="your-supported-model-id"
export BVS_AI_SERVICE_TOKEN="local-shared-token"
python3 -m ai_service
```

Then run the backend with the same `BVS_AI_SERVICE_TOKEN` and `BVS_AI_SERVICE_URL=http://localhost:8081`. To run the backend without AI, set `BVS_AI_ENABLED=false`.

## Test

```bash
python3 -m unittest discover -s tests -v
```

## Internal endpoints

- `GET /internal/v1/health`
- `POST /internal/v1/query-intents`

Never expose these endpoints directly to browsers or the public internet. Production must set a non-empty shared token and enforce private network access.

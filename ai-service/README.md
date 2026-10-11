# Bhumi Vikas AI Service

This top-level service isolates all LLM/provider-specific concerns from the main Spring backend. It currently performs bilingual query-intent extraction only. It does **not** decide applicable law, retrieve final citations, perform fee/setback calculations or expose a public browser API.

## Boundary

- `ai-service`: provider credentials, prompts, structured model calls, translation/intent proposals.
- `backend`: public API, rule corpus, deterministic retrieval, unit validation, calculations, citations and safe fallback behavior.
- `frontend`: calls only the backend.

The backend treats this service as optional. If it is disabled or unavailable, the deterministic English/Hindi parser and Lucene rule search continue to work.

## Run locally

```bash
export ANTHROPIC_API_KEY="..."
export ANTHROPIC_MODEL="your-supported-model-id"
export BVS_AI_SERVICE_TOKEN="local-shared-token"
../backend/mvnw spring-boot:run
```

Run that command from `ai-service/`. In a second terminal, run the backend with matching `BVS_AI_SERVICE_TOKEN` and `BVS_AI_SERVICE_URL=http://localhost:8081`.

Never commit provider keys or expose `/internal/**` directly to the public internet. Configure an internal token and network policy outside local development.

# Bhumi Vikas Backend

The backend is the public application API and deterministic legal-processing boundary. It contains no LLM provider SDK/client, provider prompt, model selection or provider API key.

## Package structure

```text
org.springboot.backend
├── api/             HTTP controllers and transport DTOs
├── application/     use-case services called by controllers
├── calc/            deterministic unit/calculation utilities
├── config/          web and runtime configuration
├── integration/ai/  small internal HTTP contract to ../ai-service
├── retrieval/       local deterministic intent fallback and Lucene retrieval
└── rules/           rule corpus, cleaning, chunking and version metadata
```

Dependency direction is `api -> application -> retrieval/rules`. Provider-specific AI logic exists only in the top-level `ai-service/`. The frontend calls this backend, never the AI service directly.

## Run

From this folder:

```bash
./mvnw spring-boot:run
```

By default the backend attempts `http://localhost:8081` for optional query-intent analysis and falls back to its deterministic parser if unavailable. To run without any AI call:

```bash
BVS_AI_ENABLED=false ./mvnw spring-boot:run
```

For a separately running AI service, configure `BVS_AI_SERVICE_URL` and set the same `BVS_AI_SERVICE_TOKEN` in both processes. No provider key belongs in the backend environment.

## Current endpoints

- `GET /api/health`
- `GET /api/rules`
- `GET /api/rules/{ruleNumber}`
- `POST /api/search`

The current API retrieves rule chunks and metadata. It does not yet provide a final legal-answer endpoint or deterministic fee/setback calculators.

# Bhumi Vikas Sahayak Backend

The backend is the public API and deterministic legal-processing boundary. It is built with Java 25 and Spring Boot. It owns the rule corpus, query validation, Lucene retrieval, future legal calculations/citations, and the optional internal connection to the Python AI service.

It contains no provider prompt, provider API key or provider-specific LLM client.

## Current capabilities

- Loads all 106 supplied rule records at startup.
- Cleans running PDF headers while retaining page positions.
- Detects embedded inserted rules.
- Builds an in-memory Lucene BM25 index.
- Extracts basic Hindi/English topics and measurements offline.
- Optionally accepts intent proposals from `../ai-service` and validates proposed numbers.
- Falls back to deterministic parsing when the AI service is unavailable.

Current endpoints:

- `GET /api/health`
- `GET /api/rules`
- `GET /api/rules/{ruleNumber}`
- `POST /api/search`
- `POST /api/ask` - `{question, language?: "auto"|"en"|"hi"}` -> `{status, language, answer, citations[], missingInputs[], calc, rulesVersion, warnings[]}`

Every AI answer is verified before it is shown (`citation/AnswerVerifier`): each quote must be an exact
substring of its rule's text, every rule number must exist and every number must appear in the cited
rules or the question. A failed check is retried once, then returned as `not_found`.

Legal calculators and PostgreSQL persistence are not implemented yet.

## Package structure

```text
org.springboot.backend
├── api/             public controllers and transport DTOs
├── application/     use-case services
├── calc/            deterministic units/calculation utilities
├── citation/        verifies AI quotes, rule numbers and numbers against the rule text
├── config/          web/runtime configuration
├── integration/ai/  internal HTTP contract to the Python service
├── retrieval/       offline normalization and Lucene retrieval
└── rules/           corpus loading, cleaning, chunking and version metadata
```

Dependency direction is `api -> application -> retrieval/rules`. The frontend calls only this service.

## Prerequisites

- Java Development Kit 25.
- No global Maven installation is required; Maven Wrapper is included.
- Track 1 data must remain at the default relative path or `BVS_DATA_DIR` must be set.

Check Java:

```text
java --version
```

## Run without AI

### macOS or Linux

```bash
cd backend
BVS_AI_ENABLED=false ./mvnw spring-boot:run
```

### Windows PowerShell

```powershell
Set-Location backend
$env:BVS_AI_ENABLED = "false"
.\mvnw.cmd spring-boot:run
```

### Windows Command Prompt

```bat
cd backend
set BVS_AI_ENABLED=false
mvnw.cmd spring-boot:run
```

The backend starts at `http://localhost:8080`.

## Run with the Python AI service

Start `../ai-service` first and configure the same internal token in both processes.

### macOS or Linux

```bash
cd backend
export BVS_AI_ENABLED="true"
export BVS_AI_SERVICE_URL="http://localhost:8081"
export BVS_AI_SERVICE_TOKEN="replace-with-the-same-random-secret"
./mvnw spring-boot:run
```

### Windows PowerShell

```powershell
Set-Location backend
$env:BVS_AI_ENABLED = "true"
$env:BVS_AI_SERVICE_URL = "http://localhost:8081"
$env:BVS_AI_SERVICE_TOKEN = "replace-with-the-same-random-secret"
.\mvnw.cmd spring-boot:run
```

### Windows Command Prompt

```bat
cd backend
set BVS_AI_ENABLED=true
set BVS_AI_SERVICE_URL=http://localhost:8081
set BVS_AI_SERVICE_TOKEN=replace-with-the-same-random-secret
mvnw.cmd spring-boot:run
```

## Test

macOS/Linux:

```bash
cd backend
./mvnw test
```

Windows:

```powershell
Set-Location backend
.\mvnw.cmd test
```

Tests do not require a live AI provider.

## Example API requests

macOS/Linux:

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/rules/56
curl -X POST http://localhost:8080/api/search \
  -H 'Content-Type: application/json' \
  -d '{"question":"My house faces a 10 m road. What front open space is required?"}'
```

Windows PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8080/api/rules/56
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/search `
  -ContentType "application/json" `
  -Body '{"question":"My house faces a 10 m road. What front open space is required?"}'
```

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | Backend HTTP port. |
| `BVS_DATA_DIR` | `../track-1-bhumi-vikas-sahayak/data` | Rule-data directory. |
| `BVS_PDF_FILE` | supplied English PDF | Official source PDF. |
| `BVS_CORS_ORIGINS` | `http://localhost:5173` | Allowed frontend origin(s). |
| `BVS_AI_ENABLED` | `true` | Enable optional internal AI intent call. |
| `BVS_AI_SERVICE_URL` | `http://localhost:8081` | Python AI-service URL. |
| `BVS_AI_SERVICE_TOKEN` | empty | Shared internal token; required outside local development. |
| `BVS_AI_SERVICE_TIMEOUT` | `5s` | Internal request timeout. |

Provider API keys must never be configured in the backend.

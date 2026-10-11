# Bhumi Vikas Sahayak AI Service

The AI service is a Python-only internal process that isolates provider credentials, prompts and structured language-model calls from the Java backend. It uses only the Python standard library at runtime; no `pip install` is required.

## Responsibilities

The service may:

- Detect Hindi or English.
- Translate a question into search-oriented English.
- Propose legal topic codes and explicitly stated measurements.
- Provide provider/model metadata to the backend.

The service must not:

- Decide which rule legally applies.
- Choose an authoritative table row.
- Calculate fees, setbacks, FAR or other legal values.
- Create authoritative source excerpts or citations.

The Java backend validates every response and falls back to deterministic parsing when this service is unavailable.

## Structure

```text
ai_service/
├── __main__.py             command-line entry point
├── app.py                  internal HTTP server and token validation
├── config.py               environment configuration
├── models.py               backend-compatible response model
├── providers/
│   ├── base.py             provider protocol and safe exception
│   └── anthropic.py        Anthropic HTTP adapter
└── services/
    └── query_intent.py     prompt, schema and measurement validation
tests/                      standard-library unit/API tests
```

## Prerequisites

- Python 3.11 or newer.
- `ANTHROPIC_API_KEY` and a currently supported `ANTHROPIC_MODEL` value.
- A random shared `BVS_AI_SERVICE_TOKEN` matching the backend.

No virtual environment is required because runtime and tests use only the standard library. You may still use one if preferred.

## Run on macOS or Linux

From the repository root:

```bash
cd ai-service
export ANTHROPIC_API_KEY="your-api-key"
export ANTHROPIC_MODEL="your-supported-model-id"
export BVS_AI_SERVICE_TOKEN="replace-with-a-random-secret"
python3 -m ai_service
```

## Run on Windows PowerShell

From the repository root:

```powershell
Set-Location ai-service
$env:ANTHROPIC_API_KEY = "your-api-key"
$env:ANTHROPIC_MODEL = "your-supported-model-id"
$env:BVS_AI_SERVICE_TOKEN = "replace-with-a-random-secret"
py -3 -m ai_service
```

If the Python Launcher is unavailable, replace `py -3` with `python`.

## Run on Windows Command Prompt

```bat
cd ai-service
set ANTHROPIC_API_KEY=your-api-key
set ANTHROPIC_MODEL=your-supported-model-id
set BVS_AI_SERVICE_TOKEN=replace-with-a-random-secret
py -3 -m ai_service
```

The service binds to `127.0.0.1:8081` by default.

## Test

macOS/Linux:

```bash
cd ai-service
python3 -m unittest discover -s tests -v
```

Windows PowerShell or Command Prompt:

```text
cd ai-service
py -3 -m unittest discover -s tests -v
```

Tests use a fake provider and make no external API calls.

## Internal endpoints

- `GET /internal/v1/health`
- `POST /internal/v1/query-intents`

Example health check:

macOS/Linux:

```bash
curl http://localhost:8081/internal/v1/health
```

Windows PowerShell:

```powershell
Invoke-RestMethod http://localhost:8081/internal/v1/health
```

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `AI_SERVICE_HOST` | `127.0.0.1` | Bind address. |
| `AI_SERVICE_PORT` | `8081` | Internal HTTP port. |
| `BVS_AI_SERVICE_TOKEN` | empty | Shared token; required outside local development. |
| `ANTHROPIC_API_KEY` | empty | Provider credential. |
| `ANTHROPIC_MODEL` | empty | Supported provider model identifier. |
| `AI_PROVIDER_TIMEOUT_SECONDS` | `90` | Provider request timeout. |

Never commit secrets or expose this service directly to the browser/public internet. Production should require a non-empty token and private network access.

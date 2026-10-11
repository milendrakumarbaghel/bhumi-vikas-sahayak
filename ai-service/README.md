# Bhumi Vikas Sahayak AI Service

The AI service is a Python-only internal process that isolates provider credentials, prompts and structured language-model calls from the Java backend. It uses only the Python standard library at runtime; no `pip install` is required.

## Responsibilities

The service may:

- Detect Hindi or English.
- Translate a question into search-oriented English.
- Propose legal topic codes and explicitly stated measurements.
- Provide provider/model metadata to the backend.
- Word a short answer (English or plain Hindi) from rule extracts the backend supplies,
  citing each statement as a chunk ID plus a verbatim quote. The backend verifies every
  quote against the rule text before anything is shown.
- Run the deterministic rule calculators in `ai_service/calculators/` (plain Python, no
  language model): front/rear open space (Rule 56), permission and application fees
  (Rule 21), maximum built-up area from FAR (Rule 61) and farm-house limits (Rule 17).

The service must not:

- Decide which rule legally applies.
- Let the language model choose a table row or do arithmetic: calculators do the maths and
  the model only words their result.
- Create authoritative citation metadata (rule number, title, pages); the backend attaches these.

### Calculators

Every table value in `calculators/tables.py` was checked against the official English PDF
(`track-1-bhumi-vikas-sahayak/resources/MP_Bhumi_Vikas_Rules_2012_upto_2025-03-06_EN.pdf`)
and cites its page: Rule 56 tables p.77-78, Rule 21 fees p.35-37, Rule 2(38) high rise p.8,
Rule 61 Table 8 p.83, Rule 17 p.34. Every result returns its inputs, matched band, formula,
assumptions and PDF page. Decisions where the printed rules are ambiguous:

- **Fee bands** (Rule 21(3)(b)) are printed as whole numbers with gaps (0-75, 76-125, ...).
  Each band runs up to and including its upper figure; anything above falls in the next band
  (75.5 sq m pays Rs. 1,250).
- **Floor count**: the rules give FAR but no ground coverage for ordinary residential plots.
  The calculator returns the maximum total built-up area (plot area x FAR) and a floor count
  at 50/60/75% coverage that is labelled "ILLUSTRATION ONLY, NOT FROM THE RULES".
- When only the plot area is given, fees are shown for the maximum built-up area under FAR,
  stated as an assumption.

The Java backend validates every response and falls back to deterministic parsing when this service is unavailable.

## Structure

```text
ai_service/
├── __main__.py             command-line entry point
├── app.py                  internal HTTP server and token validation
├── config.py               environment configuration
├── models.py               backend-compatible response model
├── calculators/
│   ├── tables.py           rule table values, each citing its PDF page
│   └── calculators.py      setback, fee, FAR and farm-house calculators
├── providers/
│   ├── base.py             provider protocol and safe exception
│   └── anthropic.py        Anthropic HTTP adapter
└── services/
    ├── query_intent.py     prompt, schema and measurement validation
    └── answer.py           grounded-answer prompt and schema
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
- `POST /internal/v1/answers` - `{question, language, chunks[], facts?, calculation?}` -> `{status, answer, citations[{chunkId, quote}], missingInputs[], calculations}`. If neither `calculation` nor `facts` is sent, the service derives facts from the question (query-intent analysis) and runs the calculators itself.
- `POST /internal/v1/calculations` - `{topics[], plotArea?, roadWidth?, buildingHeight?, builtUpArea?, storeys?, buildingUse?, commercialCategory?, groundCoveragePercent?}` -> `{calculations[], missingInputs[], notApplicable[]}`. Measures are `{value, unit}` with area units `sqm|sqft|sqyd|ha` and length units `m|ft`. No language model is called, so this works without an API key.

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
| `ANTHROPIC_MODEL` | `claude-opus-5-5` | Supported provider model identifier. |
| `AI_PROVIDER_TIMEOUT_SECONDS` | `90` | Provider request timeout. |

Never commit secrets or expose this service directly to the browser/public internet. Production should require a non-empty token and private network access.

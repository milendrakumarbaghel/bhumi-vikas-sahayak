# REST API Contract

Base path: `/api/v1`. JSON is UTF-8. Dates use ISO `YYYY-MM-DD`; timestamps use ISO-8601 with offset/UTC. Decimal legal measurements are JSON numbers plus explicit units. The server generates a request/answer ID if the client does not supply an idempotency key.

## 1. Ask a question

`POST /api/v1/questions:answer`

Request:

```json
{
  "question": "My house faces a 10 m road and is 9 m high. What front setback is required?",
  "language": "AUTO",
  "asOfDate": "2026-10-11",
  "facts": {
    "occupancy": "RESIDENTIAL",
    "roadWidth": { "value": 10, "unit": "METER" },
    "buildingHeight": { "value": 9, "unit": "METER" }
  }
}
```

Validation:

- `question`: required, trimmed, 3-2000 Unicode characters.
- `language`: `AUTO`, `EN` or `HI`.
- `asOfDate`: optional; defaults in Asia/Kolkata and cannot silently exceed the latest verified corpus date.
- `facts`: optional typed hints. Server extraction never overrides explicit user facts without reporting a conflict.

Answered response (`200`):

```json
{
  "answerId": "uuid",
  "status": "ANSWERED",
  "language": "EN",
  "asOfDate": "2026-10-11",
  "corpusVersion": "mp-bvr-2012+2025-03-06+amendments-2026-08-28",
  "answer": "For a residential building up to 12.5 m high facing a road wider than 9 m and up to 12 m, the minimum front open space is 3.6 m.",
  "assumptions": [
    "The building is residential and 9 m high.",
    "The stated 10 m is the street width facing the plot."
  ],
  "calculation": {
    "code": "RESIDENTIAL_FRONT_OPEN_SPACE",
    "version": "1",
    "inputs": [
      { "name": "roadWidth", "value": 10, "unit": "METER" },
      { "name": "buildingHeight", "value": 9, "unit": "METER" }
    ],
    "matchedBand": "> 9 m and <= 12 m",
    "result": { "value": 3.6, "unit": "METER" }
  },
  "citations": [
    {
      "citationId": "c1",
      "rule": "56",
      "subrule": "56(1)(a)",
      "title": "Residential Buildings",
      "part": "Part V: General Building Requirements",
      "pages": { "start": 77, "end": 77 },
      "excerpt": "More than 9.0 meters and up to 12 meters 3.6 meters",
      "sourceTitle": "M.P. Bhumi Vikas Rules, 2012",
      "sourceLanguage": "EN",
      "sourceVersionDate": "2025-03-06",
      "sourceUrl": "/api/v1/sources/{sourceId}/content?page=77"
    }
  ],
  "warnings": [],
  "disclaimer": "Information support only; the competent authority decides applicability and approval."
}
```

Other successful semantic statuses also return HTTP `200` so clients render them explicitly:

```json
{
  "answerId": "uuid",
  "status": "NEEDS_INPUT",
  "language": "EN",
  "asOfDate": "2026-10-11",
  "prompt": "What is the proposed built-up area, intended use, and is the building high-rise?",
  "requiredFacts": [
    { "name": "builtUpArea", "type": "MEASUREMENT", "unit": "SQUARE_METER" },
    { "name": "occupancy", "type": "ENUM" },
    { "name": "highRise", "type": "BOOLEAN" }
  ],
  "citations": [],
  "disclaimer": "Information support only; the competent authority decides applicability and approval."
}
```

```json
{
  "answerId": "uuid",
  "status": "NOT_FOUND_IN_RULES",
  "language": "EN",
  "asOfDate": "2026-10-11",
  "answer": "The supplied Bhumi Vikas Rules do not provide current Kolar Road land market rates.",
  "searchedCorpusVersion": "...",
  "citations": [],
  "disclaimer": "No rule answer was found; no legal conclusion has been made."
}
```

`NEEDS_REVIEW` includes the supported portions/citations and a `reviewReason` such as `EXTERNAL_RULE_CONTROLS`, `POSSIBLE_CONFLICT`, `AMENDMENT_NOT_VERIFIED` or `CALCULATOR_NOT_MODELED`.

## 2. Retrieve source metadata/content

`GET /api/v1/sources/{sourceId}` returns public provenance, checksum, language, authority, dates and corpus memberships.

`GET /api/v1/sources/{sourceId}/content?page=77` returns/streams only an approved public PDF or redirects to a trusted immutable public source. Validate `page` against stored page count. Set safe `Content-Type`, `Content-Disposition`, CSP and caching headers.

`GET /api/v1/rules/{ruleNumber}?asOfDate=2026-10-11&language=EN` returns effective provision metadata and exact text. It must return all matching inserted/sub-provisions rather than guessing when a rule identifier is ambiguous.

## 3. Calculator API

`POST /api/v1/calculations/{calculatorCode}:evaluate`

This endpoint lets the React form perform explicit, explainable calculations without an LLM. Example calculator codes:

- `RESIDENTIAL_FRONT_OPEN_SPACE`
- `RESIDENTIAL_REAR_OPEN_SPACE`
- `BUILDING_PERMISSION_FEE`
- `FARMHOUSE_LIMITS`
- `SANCTION_DURATION`
- `ELECTRIC_LINE_CLEARANCE`

Request includes `asOfDate` and calculator-specific typed `inputs`. Response includes definition/version, normalized inputs, matched band/formula, result, warnings and citations. Missing facts return `422` with field-level errors; an unmodeled legal case returns `409` code `CALCULATION_NEEDS_REVIEW`.

## 4. Feedback

`POST /api/v1/answers/{answerId}/feedback`

```json
{
  "helpful": false,
  "reason": "WRONG_NUMBER",
  "comment": "Check the boundary at exactly 9 m."
}
```

Allowed reasons are controlled values such as `WRONG_RULE`, `WRONG_NUMBER`, `MISSING_CITATION`, `UNCLEAR`, `LANGUAGE_QUALITY`, `OUTDATED` and `OTHER`. Limit comment length and warn against personal data.

## 5. Operational endpoints

- `GET /actuator/health/liveness`: process health only.
- `GET /actuator/health/readiness`: database, active corpus and required AI configuration.
- `GET /api/v1/meta`: supported languages, active corpus/version date, known cutoff/warnings and available calculators.

Detailed health, metrics and ingestion APIs require operator authorization and must not expose credentials/provider payloads.

## 6. Administrative ingestion (protected)

- `POST /api/v1/admin/corpora` creates a draft version.
- `POST /api/v1/admin/corpora/{id}/ingestions` starts/imports approved repository assets.
- `GET /api/v1/admin/ingestions/{id}` reports validation counts/errors.
- `POST /api/v1/admin/corpora/{id}:review` records maker review.
- `POST /api/v1/admin/corpora/{id}:activate` requires a distinct checker/role and atomically activates.
- `POST /api/v1/admin/corpora/{id}:retire` prevents future selection but retains traceability.

For the event MVP these may be a secured CLI instead, but the domain service, validation and audit requirements remain the same.

## 7. Error format

Transport/system failures use `application/problem+json`:

```json
{
  "type": "https://example.invalid/problems/validation-error",
  "title": "Request validation failed",
  "status": 422,
  "detail": "One or more fields are invalid.",
  "instance": "/api/v1/questions:answer",
  "requestId": "uuid",
  "code": "VALIDATION_ERROR",
  "errors": [
    { "field": "facts.roadWidth.value", "code": "MUST_BE_POSITIVE" }
  ]
}
```

Use `400` malformed JSON, `401/403` authentication/authorization, `404` unknown resource, `409` version/state conflict, `413` oversized request, `422` semantic validation, `429` rate limit, `502` provider failure and `503` unavailable dependency. Never include stack traces, prompts, secrets or SQL in responses.

## 8. Compatibility and generated types

- Publish an OpenAPI 3.1 document from Spring and validate it in CI.
- Generate or verify TypeScript DTOs from the contract to prevent frontend/backend drift.
- Additive fields are allowed within v1; breaking semantic/schema changes require `/api/v2`.
- Enumerations must include a safe client fallback (`UNKNOWN`) or version negotiation strategy.

## 9. Internal AI-service contract

The following endpoint is service-to-service only and is not part of the browser API:

`POST http://ai-service/internal/v1/query-intents`

The backend sends `{ "question": "..." }` plus `X-Internal-Token` when configured. The service returns proposed language, English translation, search terms, topic codes, raw explicitly stated measurements, building use, explicit rule references and model identifier. It does not return a legal answer or citations.

The backend must independently verify that every proposed number occurs in the original question, normalize units, ignore unknown topics and fall back to deterministic parsing on timeout, invalid response, authorization failure or service unavailability. The frontend must never call this endpoint or receive its token/provider configuration.

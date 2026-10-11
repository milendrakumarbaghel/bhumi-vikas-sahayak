# Existing System Audit

## Executive assessment

The repository now has three explicit runtime boundaries:

- `frontend/`: React 19 + TypeScript + Vite user interface (still the starter UI).
- `backend/`: public Spring Boot API under `org.springboot.backend`, PostgreSQL/Flyway persistence, rule import/retrieval, citation validation and deterministic calculation services.
- `ai-service/`: separate Python internal service containing LLM prompts, provider credentials and provider HTTP integration.

The source-data, database schema, import, retrieval and assistant API foundations are usable and tested. The frontend, broader calculators, grounded generation, production amendment activation and operational hardening remain incomplete.

## Current implementation status

| Area | Evidence | Status | Assessment |
|---|---|---:|---|
| React frontend | `frontend/src/App.tsx`, Vite/TypeScript config | Partially completed | Toolchain exists; screen is still the Vite starter and does not call the API. |
| Backend structure | `config/`, `controller/`, `dto/`, `entity/`, `repository/`, `service/`, `service/impl/`, `exception/`, `util/` under `org.springboot.backend` | Completed foundation | Matches the requested layered package structure. |
| Rule catalogue API | `GET /api/v1/rules`, `GET /api/v1/rules/{ruleNumber}` | Completed foundation | Returns imported rules and stored passages. |
| Assistant API | `POST /api/v1/questions:answer` | Partially completed | Returns explicit answer statuses, citations, warnings and deterministic calculation metadata; grounded generated prose remains limited. |
| Corpus loading | `RuleImportService`, `JsonlParser`, bundled classpath JSONL | Completed foundation | Idempotently imports all 106 top-level records into PostgreSQL at startup. Embedded inserted rules are not yet stored as independent passages. |
| Retrieval | `RuleRetrievalService`, `RulePassageRepository` | Partially completed | Database-backed term/rule-reference matching works; ranked PostgreSQL FTS/hybrid/vector retrieval remains missing. |
| Hindi/English query normalization | deterministic fallback plus optional internal AI intent client | Partially completed | Seed Hindi terms and numeric extraction work; broad bilingual coverage/evaluation is missing. |
| AI separation | Top-level Python `ai-service/`; Java `AiService` interface and `ClaudeAiService` internal HTTP adapter | Completed architecture boundary | Despite its historical name, the Java adapter has no provider prompt, key or direct Claude API call. |
| AI service behavior | Python standard-library HTTP service, internal `POST /internal/v1/query-intents`, provider protocol and Anthropic adapter | Partially completed | Intent extraction only. No grounded answer-generation or embedding endpoint yet. |
| Failure behavior | Backend falls back locally if AI service is disabled/unavailable/invalid | Completed foundation | Unsupported questions return `NOT_FOUND_IN_RULES` without citations. |
| Numerical validation | AI-proposed numbers are rechecked; `CalculationService` uses `BigDecimal` for the Rule 56 front-open-space bands | Partially completed | Fee, FAR, rear/side setback, duration and clearance calculators remain missing. |
| Citations | `CitationValidationService` verifies excerpts against stored passage text; PDF endpoint exists | Partially completed | Claim-level citation coverage and independent page/excerpt review remain incomplete. |
| PostgreSQL | JPA entities/repositories, environment configuration and `V1__initial_schema.sql` | Completed foundation | Live migration/startup was not verified against an actual PostgreSQL instance in this audit. |
| Amendment handling | `RuleAmendment` entity and `AmendmentService` effective-date lookup/warning | Partially completed | Repository/admin lifecycle, authoritative 2026 source and maker-checker activation remain missing. |
| Backend tests | Six unit tests cover bundled corpus parsing, retrieval deduplication, citation validation, answer/calculation and not-found behavior | Partially completed | Controller and Flyway/PostgreSQL integration tests remain missing. |
| AI-service tests | Python `unittest` coverage verifies the HTTP contract, token protection, Devanagari digits and removal of hallucinated measurements | Partially completed | Live-provider, timeout/rate-limit and malformed-output coverage remains incomplete. |
| Frontend tests | ESLint config only | Missing | No component/accessibility/E2E suite. |
| Operations | Separate ports/config and service READMEs | Partially completed | No Compose, CI, container images, observability or deployment network policy. |

## Service ownership

### Backend owns

- Public browser-facing APIs and validation.
- Rule corpus import, PostgreSQL persistence and official page metadata.
- Database-backed retrieval and safe fallback parsing.
- Unit verification, deterministic legal calculators and amendment/effective-date resolution.
- Citation validation, answer status, auditing and PostgreSQL persistence.
- Calling the AI service through a narrow internal interface.

### AI service owns

- LLM provider credentials and HTTP/SDK integrations.
- Provider/model configuration, prompts and structured generation schemas.
- Hindi/English translation and query-intent proposals.
- Future embeddings/reranking and grounded-language generation, once implemented.
- No final legal decision, table-row selection, calculation or citation authority.

### Frontend owns

- Clerk query/follow-up interaction and bilingual presentation.
- Rendering only backend-validated answers, calculations, citations, warnings and safe statuses.
- It must never receive provider credentials or directly call `ai-service/`.

## Configuration and security findings

- The previous plaintext hosted PostgreSQL credential is absent from the current backend configuration. Because it was once committed/shared, it must still be rotated/revoked at the provider and repository history reviewed.
- `ANTHROPIC_API_KEY` and `ANTHROPIC_MODEL` now belong only to the AI-service process.
- `BVS_AI_SERVICE_TOKEN` protects the internal endpoint when set. Production must require a non-empty token and network-level isolation; the blank-token behavior is local-development convenience only.
- Backend logs intentionally omit user question text on AI-service failure.
- The AI-service returns a generic provider-failure message rather than forwarding provider response details.

## Source data status

Completed assets:

- 106 JSONL records: Rules 1-105 plus Rule 26-B.
- English page-separated text and official English/Hindi PDFs.
- Rule/title/part/page/text metadata.
- Seventeen seed questions including Hindi and not-found cases.

Remaining data work:

- Richer corpus version/provision relationships beyond the current initial rule/passage/source/amendment schema.
- Reviewed structured rows for numeric tables.
- Authoritative official text for the 28 August 2026 Rule 2(5)(c) amendment.
- Reviewed Hindi aliases/excerpts; legacy-font extraction must not be treated as authoritative.
- Versioned ingestion, checksums, activation and rollback.

## Completed, partial and missing summary

### Completed foundations

- Clean separation of browser API/backend from LLM/provider service.
- Flyway schema plus idempotent import of the bundled 106-record corpus.
- Database-backed rule/passage retrieval with bilingual offline query cues.
- Optional AI intent analysis with deterministic fallback.
- Unit and data-integrity regression tests for current foundations.

### Partially completed

- Spring `/api/v1` backend and layered `org.springboot.backend` package structure.
- Bilingual intent normalization and rule citations metadata.
- AI internal service and shared-token protection.
- Rule version/cutoff warning.
- Automated backend/AI-service testing.

### Missing

- Product frontend and typed v1 client.
- PostgreSQL integration tests and production-grade ranked search.
- Broader deterministic legal calculators.
- Grounded answer generation and claim-level citation coverage.
- Effective-dated amendments and maker-checker activation.
- Full security, observability, evaluation, CI and deployment setup.

## Inspection limits

Status is based on repository code and local tests. No live database, external LLM provider or government system was contacted. The official August 2026 amendment text remains unverified and must not be activated from the secondary summary link.

# Existing System Audit

## Executive assessment

The repository now has three explicit runtime boundaries:

- `frontend/`: React 19 + TypeScript + Vite user interface (still the starter UI).
- `backend/`: public Spring Boot API, deterministic rule ingestion/cleaning, Lucene retrieval, unit validation and the future calculation/citation boundary.
- `ai-service/`: separate Spring Boot internal service containing LLM prompts, provider credentials and provider HTTP integration.

The source-data and retrieval foundations are usable and tested. The complete clerk-answer workflow, PostgreSQL persistence, final grounded generation, citations UI, deterministic legal calculators and amendment activation are still missing.

## Current implementation status

| Area | Evidence | Status | Assessment |
|---|---|---:|---|
| React frontend | `frontend/src/App.tsx`, Vite/TypeScript config | Partially completed | Toolchain exists; screen is still the Vite starter and does not call the API. |
| Backend structure | `api/`, `application/`, `calc/`, `config/`, `integration/ai/`, `retrieval/`, `rules/` | Partially completed | Clear public API/use-case/domain/integration boundaries now exist. |
| Rule catalogue API | `GET /api/rules`, `GET /api/rules/{ruleNumber}`, `GET /api/health` | Completed foundation | Exposes cleaned rule data, inserted-rule focus and corpus version. Not yet the final v1 contract. |
| Search API | `POST /api/search` | Completed foundation | Returns normalized intent plus top Lucene rule chunks. It is retrieval/debug output, not a final legal answer. |
| Corpus loading | `RuleRepository`, `RuleTextCleaner`, `RuleChunker` | Completed foundation | Loads 106 JSONL records, removes running headers, tracks pages and detects six embedded inserted rules. |
| Lexical retrieval | `RuleSearch`, `Retriever`, `Topic` | Completed foundation | In-memory Lucene BM25 plus explicit/topic-pinned rules; not PostgreSQL/hybrid/vector retrieval. |
| Hindi/English query normalization | deterministic fallback plus optional internal AI intent client | Partially completed | Seed Hindi terms and numeric extraction work; broad bilingual coverage/evaluation is missing. |
| AI separation | Top-level `ai-service/`; backend only has `AiIntentClient` HTTP adapter | Completed architecture boundary | Provider prompt/key/client no longer exist in `backend/`. Frontend never calls AI service directly. |
| AI service behavior | Internal `POST /internal/v1/query-intents`, provider-neutral interface and Anthropic adapter | Partially completed | Intent extraction only. No grounded answer-generation or embedding endpoint yet. |
| Failure behavior | Backend falls back locally if AI service is disabled/unavailable/invalid | Completed foundation | Retrieval remains usable without an LLM. Final answer safe-status behavior remains missing. |
| Numerical validation | Backend rejects AI-proposed measurements not present in the question; unit conversions exist | Partially completed | Legal fee/setback/FAR/duration/clearance calculators are missing; current utility uses `double`, not final `BigDecimal` legal calculations. |
| Citations | Rule/chunk/page metadata exists in retrieval results | Partially completed | No claim-to-citation validator, exact excerpt contract, official PDF viewer or final answer mapping. |
| PostgreSQL | No current datasource/JPA/Flyway configuration in the generated backend | Missing | The earlier exposed datasource configuration is no longer present, but the target database model/migrations are not implemented. |
| Amendment handling | Version warning constants identify the unverified 28 August 2026 issue | Partially completed | No effective-dated provision schema, authoritative amendment source or activation workflow. |
| Backend tests | 44 tests for retrieval, data loading, chunking and cleaning | Partially completed | Useful foundation; controller/integration/calculator/citation tests remain missing. |
| AI-service tests | Test verifies hallucinated measurements are removed | Partially completed | Provider/controller/auth/failure/contract coverage remains thin. |
| Frontend tests | ESLint config only | Missing | No component/accessibility/E2E suite. |
| Operations | Separate ports/config and service READMEs | Partially completed | No Compose, CI, container images, observability or deployment network policy. |

## Service ownership

### Backend owns

- Public browser-facing APIs and validation.
- Rule corpus loading, cleaning, chunking and official page metadata.
- Deterministic lexical retrieval and safe fallback parsing.
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

- PostgreSQL/Flyway source, corpus, provision, chunk and amendment schema.
- Reviewed structured rows for numeric tables.
- Authoritative official text for the 28 August 2026 Rule 2(5)(c) amendment.
- Reviewed Hindi aliases/excerpts; legacy-font extraction must not be treated as authoritative.
- Versioned ingestion, checksums, activation and rollback.

## Completed, partial and missing summary

### Completed foundations

- Clean separation of browser API/backend from LLM/provider service.
- Rule-file ingestion, cleaning, embedded-rule discovery and chunking.
- In-memory lexical retrieval with bilingual offline query cues.
- Optional AI intent analysis with deterministic fallback.
- Unit and data-integrity regression tests for current foundations.

### Partially completed

- Spring backend API and layered package structure.
- Bilingual intent normalization and rule citations metadata.
- AI internal service and shared-token protection.
- Rule version/cutoff warning.
- Automated backend/AI-service testing.

### Missing

- Product frontend and typed v1 client.
- PostgreSQL entities/migrations/ingestion/search.
- Final `questions:answer` orchestration and explicit safe statuses.
- Deterministic legal calculators.
- Grounded answer generation and citation validation.
- Effective-dated amendments and maker-checker activation.
- Full security, observability, evaluation, CI and deployment setup.

## Inspection limits

Status is based on repository code and local tests. No live database, external LLM provider or government system was contacted. The official August 2026 amendment text remains unverified and must not be activated from the secondary summary link.

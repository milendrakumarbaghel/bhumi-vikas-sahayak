# Architecture and Data Model

## 1. Target architecture

```text
React + TypeScript SPA
  ├─ query form / language selection / follow-up inputs
  ├─ answer, calculation and citation components
  └─ typed REST client
             │ HTTPS JSON
Spring Boot API
  ├─ QueryController / SourceController / FeedbackController
  ├─ AnswerOrchestrator
  │    ├─ deterministic fallback normalizer / HybridRetriever
  │    ├─ Applicability/EffectiveDateResolver
  │    ├─ CalculationService
  │    └─ CitationValidator
  ├─ internal AiServiceClient (HTTP contract only)
  ├─ Ingestion/Admin services
  └─ JPA repositories + JDBC/vector query adapter
        │                         │ internal HTTP
        │                         ▼
        │                 Separate AI service
        │                   ├─ language/intent prompts
        │                   ├─ embedding/reranking adapters
        │                   ├─ grounded generation schema
        │                   └─ provider clients and secrets
        │                         │
        │                         ▼
        │                 Configured AI providers
        │
PostgreSQL
  ├─ source/corpus/provision/chunk/amendment tables
  ├─ pgvector embeddings (preferred) + PostgreSQL full-text index
  ├─ structured calculation tables
  └─ answer trace / feedback / audit records
```

The LLM is not a database and not a calculator. The main backend owns public orchestration, applicability, version selection, numeric logic and citation validation. PostgreSQL owns durable sources, versions, searchable chunks and audit metadata. The separate AI service owns provider-specific code and may only propose structured intent or grounded wording. React only calls the main backend.

## 2. Backend package boundaries

Use feature-oriented packages under the existing `org.springboot.backend` root (or rename the root in a deliberate separate migration):

```text
api/            controllers, request/response DTOs, exception mapping
application/    use cases and answer orchestration
integration/ai/ narrow internal HTTP client; no prompts/provider credentials
answer/         statuses and grounded answer policies
retrieval/      keyword/vector queries, ranking, context construction
calculation/    fee, setback, FAR, duration and clearance calculators
corpus/         ingestion, parsing, source/version/provision entities
amendment/      effective dating, supersession, activation workflow
language/       detection, locale terminology and translation boundary
citation/       excerpt matching and claim-to-source validation
feedback/       feedback persistence
config/         security, CORS, Jackson and observability
```

Provider implementations, prompt templates and model credentials belong under the separate `ai-service/` root:

```text
org.springboot.aiservice
api/            internal-only endpoints and internal-token checks
application/    intent/translation/grounded-generation use cases
llm/            provider-neutral contract and provider adapters
embedding/      future embedding/reranking adapters
config/         provider-only configuration
```

Rules:

- Controllers validate transport DTOs and delegate; they do not query JPA directly.
- `AnswerOrchestrator` is transactional only where persistence is needed; calls to `ai-service/` must not hold a database transaction open.
- The backend knows only the internal service contract. Provider/model swapping happens inside `ai-service/`.
- Calculator interfaces accept typed domain inputs and an explicit rule-set version.
- Repositories return projections/domain objects; JPA entities do not cross the API boundary.
- Use `ProblemDetail` (RFC 9457-style) for API errors.

## 3. Frontend structure

Recommended modules within the existing React/Vite application:

```text
src/
  api/           generated/handwritten typed client and error mapping
  components/    QueryForm, AnswerCard, CitationCard, CalculationBreakdown
  features/      ask, source-viewer, feedback
  i18n/          en/hi typed strings and glossary
  models/        API response discriminated unions
  pages/         AssistantPage (single primary workflow)
  styles/        tokens, print and responsive rules
  test/          fixtures and test setup
```

Model `AnswerResponse` as a TypeScript discriminated union on `status`, so the UI must handle `ANSWERED`, `NEEDS_INPUT`, `NOT_FOUND_IN_RULES`, `NEEDS_REVIEW` and `ERROR` explicitly. Never use optional fields to blur these states.

## 4. PostgreSQL capabilities

### Required extensions and indexes

- Prefer `vector`/pgvector for embeddings if supported by the configured PostgreSQL provider.
- Always create PostgreSQL full-text (`tsvector`) and trigram indexes for canonical English text, rule numbers and bilingual aliases. Hybrid retrieval must still work if the embedding provider is temporarily unavailable.
- Use UUID primary keys and `timestamptz` audit columns.
- Use Flyway migrations; production Hibernate mode is `validate`, not `update`.
- Store SHA-256 hashes for source files, chunks and exact excerpts.

### Logical schema

| Table | Essential columns | Purpose |
|---|---|---|
| `source_document` | `id`, `title`, `language`, `authority`, `document_date`, `publication_date`, `url`, `local_path`, `sha256`, `is_official` | Immutable source identity and provenance. |
| `corpus_version` | `id`, `name`, `status`, `valid_from`, `valid_to`, `cutoff_date`, `created_at`, `activated_at`, `approved_by` | Reproducible snapshot used for an answer. |
| `provision` | `id`, `canonical_key`, `rule_number`, `subrule_path`, `title`, `part`, `language`, `text`, `page_start`, `page_end`, `valid_from`, `valid_to`, `source_document_id` | Effective-dated legal unit. `canonical_key` links languages/versions. |
| `provision_relation` | `from_id`, `to_id`, `relation_type` | `AMENDS`, `SUPERSEDES`, `INSERTS`, `REFERS_TO`, `TRANSLATION_OF`. |
| `source_chunk` | `id`, `provision_id`, `corpus_version_id`, `chunk_order`, `text`, `search_vector`, `embedding`, `token_count`, `sha256` | Retrieval unit that preserves provision context. |
| `term_alias` | `id`, `language`, `term`, `canonical_term`, `rule_number`, `reviewed` | Hindi/English/mixed-language legal vocabulary and spelling variants. |
| `calculation_definition` | `id`, `code`, `version`, `rule_number`, `source_version`, `valid_from`, `valid_to`, `status`, `review metadata` | Versioned calculator release. |
| `calculation_band` | `definition_id`, dimensions/conditions as JSONB, `result` JSONB, `priority`, `source_excerpt`, `page` | Reviewed table rows; JSONB permits different calculator dimensions while Java types validate them. |
| `answer_trace` | `id`, `created_at`, `query_language`, `as_of_date`, `corpus_version_id`, `status`, model IDs, latency, normalized query hash, optional encrypted/redacted query | Reproducibility and evaluation. Retention applies. |
| `answer_citation` | `answer_id`, `chunk_id`, `claim_key`, `excerpt`, rank/scores | Exact evidence used in the response. |
| `calculation_trace` | `answer_id`, `definition_id`, validated inputs/result JSONB | Reproduce numerical answers. |
| `feedback` | `id`, `answer_id`, `rating`, `reason_code`, `comment`, `created_at` | Non-personal quality signal. |
| `ingestion_run` | `id`, `corpus_version_id`, state, counts, checksum/errors, started/completed timestamps | Idempotent ingestion audit. |

Do not store source PDFs as opaque database blobs unless deployment requires it. Store immutable files in packaged/object storage and their provenance/checksums in PostgreSQL.

## 5. Ingestion flow

1. Register source file and checksum; reject an unexplained checksum change.
2. Parse each JSONL record and validate all required fields, unique rule keys and page ranges.
3. Preserve `raw_text`; create `normalized_text` by removing known running headers without changing legal content.
4. Split at headings/sub-rules/table boundaries, keeping rule title/part/page metadata on every chunk.
5. Detect embedded inserted rules (12-A, 17-a, 25(A), 26-A, 42-A, 93(a)) and assign canonical sub-provision keys without losing their parent/source position.
6. Create lexical indexes and embeddings in batches; record model name/dimension.
7. Import curated Hindi aliases and any reviewed aligned Hindi excerpts. Do not parse legacy-font output as authoritative text automatically.
8. Run corpus validation and golden retrieval tests.
9. A reviewer compares numeric tables and amendments with official pages.
10. Activate the immutable corpus version atomically; keep the prior version rollbackable.

The importer must be idempotent by `(source sha256, parser version, embedding model/version)`.

## 6. Answer orchestration flow

1. Validate request and establish `asOfDate` in Asia/Kolkata.
2. Detect/confirm language; normalize units and legal aliases.
3. Classify intent: lookup, deterministic calculation, mixed, out-of-scope or ambiguous.
4. Resolve the active corpus and effective provisions for the requested date.
5. Run hybrid retrieval: rule/term filters, full-text ranking and vector similarity; rerank while retaining scores.
6. If calculation is required, validate extracted inputs and either request missing fields or execute a versioned Java calculator.
7. Build a bounded context containing immutable chunk IDs and exact text.
8. Call the internal AI service for structured wording whose claims reference supplied chunk IDs only.
9. Validate cited IDs, excerpt matches, effective dates, calculation consistency and claim coverage.
10. Fail closed to `NOT_FOUND_IN_RULES` or `NEEDS_REVIEW` when evidence/validation is insufficient.
11. Persist a redacted trace and return the typed response.

## 7. Deployment/configuration requirements

- Backend externalizes `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `BVS_AI_SERVICE_URL`, `BVS_AI_SERVICE_TOKEN`, allowed origins and retention settings.
- Only `ai-service/` receives provider keys/model configuration. Production requires a non-empty internal token and private network path; do not expose its internal API to browsers.
- Use separate Spring profiles for local/test/production; test uses Testcontainers PostgreSQL with pgvector where supported.
- Package official PDFs as versioned read-only resources or serve from controlled object storage.
- Do not send database secrets or entire PDFs to the browser or model provider.
- Define connection/read timeouts and circuit breakers for backend-to-AI-service and AI-service-to-provider calls. Backend must retain deterministic fallback behavior.
- Log structured IDs and status, not secrets, raw prompts or personal information.
- Use separate database roles for migrations and runtime reads/writes.

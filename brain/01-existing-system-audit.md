# Existing System Audit

## Executive assessment

The repository has the intended technology foundations and a strong source-data pack, but it does not yet implement Bhumi Vikas Sahayak. The frontend and backend are generated starter applications. PostgreSQL connectivity is configured, but there is no domain schema or persistence code. There is no retrieval, AI, bilingual answer, citation, calculation, amendment, or evaluation implementation.

## Repository evidence

| Area | Evidence found | Status | Assessment |
|---|---|---:|---|
| React application | `frontend/src/App.tsx`, `main.tsx`, CSS, Vite and TypeScript configs | Partially completed | React 19 + TypeScript + Vite boots as a scaffold. `App.tsx` is still the Vite counter/get-started UI. |
| Frontend product UX | No question form, answer view, citation component, language control, calculator inputs, history, API client or error states | Missing | No Track 1 workflow is present. |
| Backend application | One `@SpringBootApplication` launcher | Partially completed | Spring Boot wiring exists, but there are no controllers, services, DTOs, entities, repositories or exception handlers. |
| REST API | No controller classes or endpoint definitions | Missing | The frontend cannot query the backend. |
| PostgreSQL driver/JPA | PostgreSQL runtime driver and Spring Data JPA in `pom.xml` | Partially completed | Dependencies exist, but no tables, entities, repositories, migrations or seed process exist. |
| Database configuration | Datasource URL, username and password in `application.properties`; Hibernate `ddl-auto=update` | Partially completed / unsafe | Connectivity is declared but not verified. Credentials are committed in plaintext, schema changes are nondeterministic, and `open-in-view=true` is unsuitable for an explicit API boundary. |
| Security | Spring Security dependency and test dependency | Partially completed | No `SecurityFilterChain`, authentication model, CORS policy, rate limiting or authorization behavior is implemented. Default Spring Security behavior may block API calls. |
| Rule corpus | Full JSONL, sample JSONL, page-separated English text, official English/Hindi PDFs | Completed as source assets | The dataset includes 106 records: Rules 1-105 and 26-B, with rule/title/part/page range/text. |
| Corpus caveats | Data dictionary and participant guide identify flattened tables, running headers, embedded inserted rules and amendment footnotes | Completed as documentation | These caveats must be handled during ingestion and evaluation. |
| 2026 amendment | Mentioned in project documentation but not included in the JSONL/PDF corpus | Partially completed | Awareness exists; authoritative amendment content, structured supersession and effective-date logic are absent. |
| RAG / search | No embedding model, vector extension, text search, index, ingestion job or retriever | Missing | No retrieval pipeline exists. |
| LLM generation | No AI SDK/provider config, prompt, grounding checks or answer service | Missing | No generated answers exist. |
| Citations | Rule metadata exists in JSONL | Partially completed | Citation source fields exist, but nothing creates/verifies/displays citations or excerpts. |
| Hindi/English | Official PDFs and two Hindi test questions exist | Partially completed | No language detection, Devanagari UI, bilingual retrieval, translation or Hindi answer generation exists. Hindi PDF extraction is explicitly unreliable. |
| Numerical calculations | Numeric rule tables are present in the corpus | Partially completed | Values exist, but there is no deterministic fee/setback/FAR/duration/clearance calculator or boundary validation. |
| Backend tests | One `contextLoads()` test | Partially completed | Test harness exists; it does not test product behavior and may require the configured external database. |
| Frontend tests | ESLint configuration only | Missing | No unit, component, accessibility or end-to-end test framework/configuration exists. |
| Evaluation | Seventeen sample questions, including two out-of-domain questions | Partially completed | A useful seed set exists, but expected answers, grading, bilingual parity and regression automation are absent. |
| Operations | No Dockerfile, Compose, CI workflow, health/readiness design, logging policy or deployment config found | Missing | There is no reproducible full-stack operating path. |
| Project delivery docs | Root README describes event and source pack | Partially completed | It does not identify a team, provide a <=5-step application runbook, disclose app limitations, or include app architecture. No license was found. |

## Existing configuration risks

### Critical: exposed PostgreSQL credential

`backend/src/main/resources/application.properties` contains a plaintext hosted-database password. Treat it as compromised even if the database is disposable.

Required remediation before development or publication:

1. Rotate/revoke the exposed credential at the database provider.
2. Read URL, username and password from environment variables or a secret manager.
3. Commit only safe placeholders and a documented `.env.example` without secrets.
4. Review repository history and provider access logs; removing the current line alone does not remove historical exposure.
5. Use least-privilege application and migration roles.

### Database lifecycle

`spring.jpa.hibernate.ddl-auto=update` can silently mutate shared environments. The target system must use versioned Flyway migrations, with Hibernate validation in deployed environments. `spring.jpa.open-in-view=true` should be disabled so database access remains inside service transactions.

### Default security behavior

Adding Spring Security without an explicit configuration normally secures application endpoints by default. The implementation must deliberately define public/internal endpoints, CORS, CSRF behavior and any clerk authentication rather than relying on defaults.

## Data fitness and gaps

### Ready to use

- JSONL supplies `rule`, `title`, `part`, `page_start`, `page_end` and full English `text`.
- The English PDF is identified as the source of truth through 6 March 2025.
- The Hindi PDF provides an official visual reference.
- Sample questions cover retrieval, table lookup, calculations, cross-rule questions, Hindi and out-of-domain refusal.

### Must be normalized or curated

- Remove running page headers from searchable chunks while preserving original text separately.
- Segment sub-rules and tables without losing parent rule context.
- Represent inserted Rules 12-A, 17-a, 25(A), 26-A, 42-A and 93(a), which are embedded in parent records; only 26-B has its own JSONL row.
- Convert important tables into reviewed structured rows for deterministic calculations.
- Preserve amendment footnotes and link them to the exact provision changed.
- Add the 28 August 2026 Rule 2(5)(c) amendment as a separately sourced, effective-dated record after authoritative verification.
- Curate Hindi terminology/aliases because the legacy-font Hindi PDF is unsuitable as the primary machine-readable input.

## Current capability summary

### Completed

- Repository split into frontend, backend and Track 1 data/resources.
- React/TypeScript/Vite scaffold.
- Spring Boot/MVC/JPA/Security/PostgreSQL dependency scaffold.
- English rule dataset and official source PDFs.
- Data dictionary, problem statement and 17 seed questions.

### Partially completed

- PostgreSQL connection configuration.
- Backend and frontend toolchains.
- Citation metadata in the source dataset.
- Bilingual source availability.
- Numerical rule content availability.
- Minimal backend test harness and frontend linting.
- Awareness of the August 2026 amendment.

### Missing

- All user-facing product behavior.
- Domain model and database migrations.
- Corpus ingestion and version control.
- Hybrid retrieval and LLM integration.
- Citation verification and source viewer.
- Hindi/English query and answer pipeline.
- Deterministic numerical calculation engine.
- Amendment ingestion, activation and supersession.
- REST APIs and generated frontend client/types.
- Authentication/authorization decisions and security configuration.
- Meaningful automated tests, RAG evaluation and operational readiness.

## Inspection limits

This audit is static. Frontend dependencies were not installed, no live PostgreSQL connection was attempted, and no external model or amendment source was called. The participant guide PDF was visually checked at its opening page and cross-checked against the repository's Markdown guide; PDF text tooling was unavailable in the environment. These limits do not affect the implementation-status findings because the application source contains no product feature code.

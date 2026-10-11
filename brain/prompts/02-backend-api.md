# Prompt: Spring Boot Backend API and Orchestration

Work in `/Users/milendrakumarbaghel/Documents/MPSeDC/bhumi-vikas-sahayak`.

Build the Spring Boot backend API for Bhumi Vikas Sahayak using the existing Java/Spring project and the contracts in `brain/`. Read the full requirements pack and inspect current database work/migrations before editing. Preserve unrelated changes and existing agreed schemas.

Implement:

- Feature-oriented packages for API, answer orchestration, retrieval interfaces, calculation interfaces, corpus, language, citation, feedback and configuration.
- `/api/v1/questions:answer`, source/rule endpoints, calculator dispatch endpoints, feedback, `/api/v1/meta`, and health/readiness behavior from `brain/04-api-contract.md`.
- Typed request/response DTOs. Model semantic answer states explicitly: `ANSWERED`, `NEEDS_INPUT`, `NOT_FOUND_IN_RULES`, `NEEDS_REVIEW` and system errors.
- RFC-style Spring `ProblemDetail` responses with stable codes and request IDs.
- An `AnswerOrchestrator` that establishes Asia/Kolkata `asOfDate`, validates/normalizes facts, selects active corpus, calls retrieval/calculation/generation abstractions, validates citations, persists a redacted trace and fails closed.
- Exact source/citation enrichment from database records; never accept model-authored page numbers/excerpts.
- Safe Spring Security configuration: explicitly define public query/source/meta/health access and protected admin endpoints. Configure CORS from environment. Do not disable protections broadly without documenting why.
- Input size/range validation, safe errors, structured redacted logs, timeouts and provider failure mapping.
- OpenAPI 3.1 output suitable for TypeScript client generation.

Use interfaces/fakes for AI, retrieval and calculators where their implementations are not yet present. Do not add a fake confident answer path. A missing provider/evidence must yield a safe status/error.

Tests:

- Unit tests for orchestration state transitions and fact conflicts.
- MockMvc/API tests for all statuses, validation, security/CORS, source-page validation, provider timeout/malformed output and no-secret/no-stack-trace errors.
- Integration tests use Testcontainers, not a live hosted database.
- Contract tests ensure examples in `brain/04-api-contract.md` stay valid.

Deliverables:

- Compiling backend code, tests and OpenAPI contract.
- A short endpoint/run guide and requirement-ID coverage report.
- Commands run and unresolved dependencies/decisions.

Do not implement or redesign the React UI in this task. Do not change official rule data.

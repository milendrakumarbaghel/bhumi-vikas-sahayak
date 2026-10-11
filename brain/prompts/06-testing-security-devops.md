# Prompt: Testing, Security and DevOps Hardening

Work in `/Users/milendrakumarbaghel/Documents/MPSeDC/bhumi-vikas-sahayak`.

Harden and verify the existing Bhumi Vikas Sahayak implementation against `brain/06-testing-and-delivery-plan.md`. Read all requirements and inspect the entire worktree before editing. This task may add tests, test fixtures, CI and deployment/configuration files, and may make narrowly scoped fixes revealed by tests. Preserve unrelated changes.

Security first:

- Confirm the committed database password has been removed from runtime source and replaced with environment configuration. Never print or reuse it. Report that provider-side rotation/history cleanup still requires the owner if not verifiable.
- Add secret scanning and dependency/security checks appropriate to Maven and npm.
- Verify Spring Security endpoint policy, CORS, input limits, source-content headers, rate limiting, log redaction and admin authorization.
- Test prompt injection, HTML/script source text, oversized input, malformed provider output, unknown citations and SQL/search metacharacters.

Testing:

- Establish/complete backend unit, MockMvc, Testcontainers integration and migration tests.
- Establish/complete frontend unit, accessibility and Playwright tests.
- Create deterministic fake AI/embedding providers for CI; never require paid APIs or hosted PostgreSQL in CI.
- Convert the 17 sample questions into a reviewed golden evaluation format and runner. Keep uncertain expected answers marked pending review rather than guessing.
- Add calculator boundary, bilingual parity, effective-date/amendment and not-found suites.
- Validate OpenAPI against frontend types/fixtures.
- Measure and report retrieval/citation/numeric/not-found metrics with sample sizes.

Operations:

- Add reproducible local startup (for example Compose for PostgreSQL/pgvector if consistent with the implementation), safe `.env.example`, health/readiness, and <=5-step run instructions.
- Add CI for backend compile/tests, frontend lint/type-check/tests/build, migration/integration tests and secret scan.
- Document backup/restore, corpus rollback, model/provider configuration, observability and incident behavior.
- Do not deploy, publish, rotate remote credentials or write to external services without explicit authorization.

Exit report:

- Pass/fail table for every MVP acceptance gate in `brain/06-testing-and-delivery-plan.md`.
- Commands, versions and test counts run.
- Security findings by severity and fixes made.
- Remaining blockers, human review needs and operational risks.

Do not weaken a test or safe failure behavior merely to make CI green.

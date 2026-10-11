# Prompt: PostgreSQL Schema and Corpus Ingestion

Work in `/Users/milendrakumarbaghel/Documents/MPSeDC/bhumi-vikas-sahayak`.

Implement the database and corpus-ingestion foundation for Track 1 - Bhumi Vikas Sahayak. Read every document under `brain/` first, then inspect the repository and current git status. Preserve unrelated changes.

Scope:

- Spring Boot/Java and PostgreSQL only; do not introduce Node, Express or MongoDB.
- Add Flyway and create versioned migrations for source documents, corpus versions, provisions, provision relations, chunks, term aliases, calculation definitions/bands, answer/citation/calculation traces, feedback and ingestion runs as specified in `brain/03-architecture-and-data-model.md`.
- Replace unsafe schema auto-update behavior with environment/profile-based migration plus Hibernate validation. Do not put real credentials in source.
- Support pgvector only if it is available and cleanly testable; otherwise establish lexical search first and isolate vector support behind a migration/configuration boundary.
- Implement an idempotent importer for `track-1-bhumi-vikas-sahayak/data/bhumi_vikas_rules.jsonl`.
- Validate exact dataset fields (`rule`, `title`, `part`, `page_start`, `page_end`, `text`), 106 records, unique rule keys and sane page ranges.
- Preserve raw source text and create separately normalized searchable text. Remove only known running headers/spacing; never alter legal numbers/content.
- Represent embedded inserted rules (12-A, 17-a, 25(A), 26-A, 42-A and 93(a)) with canonical identities and parent lineage without fabricating page/content.
- Register official English/Hindi PDFs and source checksums/provenance. Do not parse corrupted legacy-font Hindi text as authoritative content.
- Add full-text/trigram indexes and repository/query adapters for rule number, titles, aliases and body text.
- Seed a draft corpus version consolidated through 6 March 2025. Do not mark the August 2026 amendment active.

Security requirement: `backend/src/main/resources/application.properties` currently contains a plaintext database password. Do not repeat it in output or copy it elsewhere. Replace source configuration with environment-variable placeholders and a safe local/test profile. State clearly that provider-side rotation cannot be performed from code and remains required.

Tests:

- Use Testcontainers PostgreSQL; do not test against the configured hosted database.
- Verify migrations from an empty DB, importer counts/field validation, idempotence, embedded-rule lineage, effective-date constraints, lexical lookup and rollback-safe corpus activation.
- Test malformed JSONL, duplicate rules, invalid pages and checksum mismatch.

Deliverables:

- Code/migrations/config/tests and concise run instructions.
- A summary mapping changes to FR-CITE, FR-AMD, NFR-SEC, NFR-MAINT and the schema in `brain/`.
- Exact commands/tests run and remaining limitations.

Do not implement the React UI or real LLM calls in this task.

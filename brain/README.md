# Track 1 - Bhumi Vikas Sahayak: Requirements Pack

This folder is the implementation specification for turning the current repository into a bilingual, citation-first assistant for a building-permission clerk at Bhopal Municipal Corporation.

The specification is tailored to the code and data presently in this repository:

- Frontend: React 19, TypeScript 6 and Vite 8.
- Backend: Spring Boot 4 and Java 25 for public APIs, deterministic rules, calculations, citations and persistence.
- AI service: a separate top-level Python service for provider credentials, prompts, embeddings and LLM calls.
- Database: PostgreSQL (currently configured as a hosted Neon database).
- Legal corpus: 106 JSONL records covering Rules 1-105 plus Rule 26-B, the page-separated English text, and official English/Hindi PDFs consolidated through 6 March 2025.

## Documents

1. [Existing system audit](01-existing-system-audit.md) - evidence-based inventory and completed/partial/missing classification.
2. [Product requirements](02-product-requirements.md) - users, workflows, functional requirements, bilingual behavior, calculations, citations and safeguards.
3. [Architecture and data model](03-architecture-and-data-model.md) - React/Spring/PostgreSQL design, component boundaries, schema and processing flows.
4. [API contract](04-api-contract.md) - proposed REST endpoints and typed request/response/error shapes.
5. [AI, RAG and amendment requirements](05-ai-rag-and-amendments.md) - the separate AI-service boundary, grounding, Hindi/English handling, citations, refusal behavior and versioning.
6. [Testing and delivery plan](06-testing-and-delivery-plan.md) - test pyramid, evaluation set, acceptance gates and prioritized implementation sequence.
7. [Implementation prompt index](prompts/README.md) - separate, copy-ready prompts for each workstream and a recommended execution order.
8. [Backend/AI completion handoff](07-backend-ai-completion.md) - the six implemented improvements, actual endpoints, verification results and external acceptance limits. This is the current implementation record; older specification sections can still describe future product work.

## Status vocabulary

- **Completed**: usable implementation or source asset exists in the repository.
- **Partially completed**: a framework/dependency/configuration exists, but the user-facing capability is not implemented end to end.
- **Missing**: no implementation was found in the scanned repository.

Status was updated after local unit, HTTP-contract and disposable PostgreSQL integration tests on 11 October 2026. No shared hosted database or live AI provider was used. Generated build artifacts were not treated as feature implementation.

## Product boundary

The product answers questions from the supplied public rule corpus. It must not submit applications, connect to a live government system, process personal data, or claim to make a legal decision. It points the clerk to applicable text; the clerk remains the decision-maker.

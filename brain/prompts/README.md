# Implementation Prompts

These prompts are designed to be run separately against this repository. Each prompt is scoped so an implementation agent can make progress without silently changing another workstream's contract.

## Recommended order

1. [Database and corpus ingestion](01-database-and-ingestion.md)
2. [Backend API and orchestration](02-backend-api.md)
3. [AI/RAG and bilingual grounding](03-ai-rag.md)
4. [Calculators and amendment handling](04-calculators-and-amendments.md)
5. [Frontend](05-frontend.md)
6. [Testing, security and DevOps](06-testing-security-devops.md)
7. [Full integration and release review](07-integration-release.md)

Workstreams 1 and the backend skeleton in 2 can begin in parallel only after agreeing on migrations and the OpenAPI response model. Workstreams 3 and 4 depend on the domain/schema contracts. The frontend can start against mocked OpenAPI fixtures, then integrate when the backend contract is stable.

## Shared rules for every prompt

- Read all files in `brain/` before implementation, especially the audit, requirements, architecture, API contract and test plan.
- Inspect the current worktree and preserve unrelated/user changes. Do not overwrite work from another workstream.
- Stay with React + TypeScript + Vite, Spring Boot + Java, and PostgreSQL. Do not replace them with Node/Express/MongoDB.
- Never invent a rule or calculate authoritative numeric results in an LLM.
- Never commit secrets. The currently exposed database credential must be treated as compromised and rotated by the owner/provider.
- Use official/repository source assets only. Do not activate the August 2026 amendment from a secondary summary.
- Every legal claim needs validated citations; unsupported questions must fail closed.
- Add tests proportional to every change and report what was/was not run.
- Do not claim completion if acceptance criteria in `brain/` are not met.

Each file below is a standalone prompt that can be copied into a new coding session.

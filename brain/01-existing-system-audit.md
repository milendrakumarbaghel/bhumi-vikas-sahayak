# Existing System Audit

Updated 11 October 2026 after the six backend/AI improvements. This distinguishes implemented backend behavior from deployment and legal-review acceptance.

## Runtime boundaries

- `frontend/`: React 19, TypeScript and Vite; still the starter screen.
- `backend/`: Java 25 / Spring Boot 4 under `org.springboot.backend`, Spring Security, PostgreSQL/Flyway, retrieval, amendment administration and final answer validation.
- `ai-service/`: Python-only standard-library service; intent, grounded wording, deterministic calculators and separate semantic verification.

## Current implementation status

| Area | Status | Evidence / limits |
|---|---|---|
| Frontend | Partial | Toolchain exists; no authenticated clerk/chat or administrator workflow yet. |
| API boundary | Implemented | Answer, rule, document, auth, administrator amendment/import and health APIs. |
| Corpus import | Implemented | 106 records, numbered clause/table passages, idempotent rebuild of stale chunking. |
| Retrieval | Implemented | PostgreSQL stored tsvector / GIN rank, rule/clause boosts, lexical fusion and Hindi synonym expansion. Embeddings are not implemented or required by this hybrid path. |
| Hindi/English | Partial product readiness | Intent, digits, synonym expansion and language overrides work. Offline fallback labels English source extracts rather than inventing a Hindi translation. Full bilingual UX/evaluation remains. |
| Grounded generation | Implemented with safe fallback | Python proposes prose; Java checks exact quotes, complete claim coverage, rule references and numbers. A separate evidence-only semantic call must entail every claim. |
| Semantic entailment | Implemented, not certified | Independent verification context checks scope, negation, modality, exceptions, dates and units. Same configured provider/model; no formal proof or live-provider accuracy claim. |
| Numerical calculations | Implemented current tables | Rule 42 all three Table 4 and five Table 5 rows, conjunctive site minima, conditional commercial FAR, storey/height distinction, deleted coverage and above-90-m caveats; existing fee/FAR/setback/clearance/validity/farm calculators retained. |
| Source citations | Implemented validation | Exact stored excerpts and clause metadata; amendments use their notification URL/page/version. Independent legal reviewer must still check corpus/page fidelity. |
| PostgreSQL | Verified locally | V1/V2 Flyway, schema validation, startup import and real full-text queries exercised on disposable PostgreSQL 16 containers. No shared hosted database used. |
| Amendments | Implemented lifecycle | Draft provenance, different-admin review, activation, rejection, same-scope supersession, exclusive end dates, historical evidence and audit trail. Actual August 2026 source still absent. |
| USER/ADMIN authentication | Implemented | BCrypt passwords, hashed random opaque tokens, expiry/logout/disable, account lockout and administrator-only import/amendment/user provisioning. No public registration/static import token. |
| Backend tests | Implemented regression suite | Unit/controller/contract tests, all 17 actual official sample responses, PostgreSQL/search/auth/amendment tests. Official response tests deliberately exercise deterministic fallback with a real Python calculator, not a paid live provider. |
| Python tests | Implemented regression suite | Internal HTTP contracts, fake provider, facts/units/calculators, all Rule 42 rows and independent verifier fail-closed tests. |
| Operations | Partial | Health/readiness and platform setup exist. CI, load tests, MFA/SSO, recovery, monitoring, backup/restore and deployment policy remain. |

## Ownership

Java owns application authorization, legal source selection/effective dates, provenance, citations, final acceptance and audit. Python owns provider credentials/prompts, proposed language and deterministic table functions. Java does not accept generated calculations in place of trusted calculator output. If amended evidence is selected, old base-corpus calculations are withheld until revised table logic has been reviewed.

The browser calls Java only. Authenticated PDF viewing requires a Bearer-authorized fetch/Blob or authenticated viewer; do not leak tokens into source URLs.

## Security/configuration findings

- `DB_PASSWORD` now has no embedded password fallback. A credential previously committed/shared must still be revoked/rotated at the provider; that external operation was not performed.
- All DB connection variables must be explicitly set for local runs to avoid the configured hosted URL.
- `BVS_BOOTSTRAP_ADMIN_USERNAME/PASSWORD` provision the first ADMIN; remove after setup. Existing passwords are not overwritten.
- A second ADMIN is required for independent amendment review.
- Browser APIs use application Bearer tokens; the internal `BVS_AI_SERVICE_TOKEN` is separate and must be non-empty in deployment.
- Passwords are BCrypt hashed; opaque tokens are SHA-256 hashed in PostgreSQL. Tokens expire and can be revoked.
- HTTPS, private AI networking, distributed abuse protection, recovery/MFA/SSO and operational security review remain deployment requirements.

## Source and legal limits

The supplied English/Hindi PDFs and 106-record corpus are consolidated through 6 March 2025. Rule 42 Tables 4/5 were checked against official English PDF printed pages 58-59.

The authoritative August 2026 Rule 2(5)(c) notification is not bundled. Do not invent its wording, checksum or exact effective date from secondary summaries. The warning remains until verified material is submitted, independently reviewed and activated. Submitted amendment provenance is recorded, not automatically authenticated against a government source.

Regression gold checks use the supplied rules and explicit expected statuses/facts, but are not department-signed legal certification. A live provider key was not configured, so no paid-model end-to-end quality evaluation was run.

## Still outside this change

Product frontend, typed authenticated client, expert-signed larger bilingual dataset, actual amendment acquisition, observed live-model performance, deployment/monitoring and administrator UI. See [the completion handoff](07-backend-ai-completion.md) for implementation/API/test details.

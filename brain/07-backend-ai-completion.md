# Backend / AI completion handoff

Implementation record, 11 October 2026. Scope: the six requested backend/AI improvements, not a frontend rebuild or deployment certification.

## 1. Rule 42 permitted-height / high-rise tables

`ai-service/ai_service/calculators/tables.py` contains all three Table 4 rows and all five Table 5 rows from official English PDF printed pages 58-59. `rule42_planning` evaluates every row without combining columns from different bands.

- Minimum road width, plot area and frontage must all hold for the same row.
- More than 30 m height or more than ten storeys uses Table 5. At lower proposed heights, missing storey count is reported rather than silently choosing Table 4.
- No proposed height produces a capacity catalogue, not an invented compliance result.
- Table 4 deleted ground coverage is null; Table 5 coverage is 30%.
- Above-90-m row has no printed numerical upper height, but never implies unlimited permission.
- Conditional commercial FAR 5/7 is reported with approval, TDR/Premium FAR and planning-area/CBD/TOD conditions, never applied automatically.
- NBC/fire/site-permission/access caveats accompany the results.
- Height-based setback/FAR queries route to Rule 42 instead of ordinary low-rise tables.

Java passes only re-verified extracted facts; Python returns inputs, matched rows, formula, result and caveats. The language model does not choose table rows or calculate.

## 2. Effective-dated amendment administration

Migration V2 adds provenance, review/activation metadata, exclusive end dates, successor links and append-only audit events. `AmendmentService` and `AmendmentController` implement:

1. ADMIN submits a draft with exact target clause, notification, effective date, replacement text, HTTPS source URL, SHA-256 and pages.
2. A different ADMIN reviews the official material and replacement wording.
3. ADMIN activates only an independently reviewed draft. Drafts never appear in answers.
4. Later same-scope activation retires the predecessor at the new effective date and records supersession. Overlapping whole-rule/sub-clause scopes are rejected for explicit resolution.
5. Historical `asOfDate` uses the previously activated version; current answers remove replaced base evidence and cite the applicable notification.
6. Unactivated drafts can be rejected with an audit reason. Rejected text cannot be activated or retrieved as authoritative evidence.

API prefix: `/api/v1/admin/amendments`, with list/create, `/{id}/review`, `/{id}/activate`, `/{id}/reject`, `/{id}/history`.

Important limits: checksum/URL are submitted provenance, not an automatic government-document authenticity guarantee. Administrator review must independently verify them. Arbitrary legal amendments cannot automatically rewrite calculator formulas: base calculations are withheld when amended evidence is selected until the corresponding definitions are reviewed. Original base corpus remains immutable. The actual August 2026 notification has not been supplied/activated.

## 3. Actual official sample-answer evaluation

`OfficialSampleQuestionsTest` now runs all 17 supplied questions through production intent/retrieval/orchestration, real chunked corpus and a temporary Python calculator process. It checks actual response status, validated rule citations, mandatory values/legal phrases, Hindi language metadata and not-found behavior. Questions lacking enough facts return explicit input requirements; the unverified amendment question returns review status.

This is not the former inventory/availability check. It is also not a comprehensive legal proof: expected results have not been independently department-signed. Tests deliberately disable the paid provider and exercise the safe deterministic source-excerpt/calculator path. PostgreSQL ranking is covered separately against real SQL, not mocked in that integration suite.

## 4. Stronger claim entailment

- Python generation returns ordered sentence-level `claims[{text,chunkIds}]` alongside verbatim citation proposals.
- Java checks complete answer/claim coverage, per-claim citations, exact source excerpts, rule references and numerical values.
- Java then calls `/internal/v1/claims:verify` with each claim and only its cited quotes plus deterministic calculation evidence.
- A separate evidence-only model call checks meaning, negation, modality, scope, units, dates, exclusions, conditions and bilingual translation.
- Every sequential claim ID must occur exactly once and be `entailed`. Contradiction, insufficient evidence, malformed/incomplete verdicts, unavailable model or missing coverage rejects generated prose.
- Rejected generation falls back to exact source excerpts and deterministic results; no unverified paraphrase is presented as validated.

The verifier uses the configured provider/model but a separate prompt/call/context. It reduces hallucination risk; it cannot conclusively prove arbitrary natural-language legal claims. Live-provider expert-reviewed adversarial evaluation remains required before legal pilot acceptance.

## 5. PostgreSQL full-text / hybrid retrieval and Hindi

V2 adds a stored English `tsvector` and GIN index on passage label/text. `RulePassageRepository` executes ranked `websearch_to_tsquery` / `ts_rank_cd` queries. `RuleRetrievalService` combines full-text candidates, explicit ordered rule/clause boosts and lexical matches, deduplicates passages and returns up to eight.

`QueryLexicon` expands Hindi department terms for road, plot, height, front/rear/side open space, fees, permissions, farm house, validity, documents, electric lines, lifts and FAR. Explicit out-of-corpus property-tax/market-price questions remain safe not-found cases.

This is full-text plus lexical/rule-aware hybrid ranking, not a claim that vector embeddings exist. Add vectors/reranking only if a larger evaluation shows a benefit. Hindi morphology/code-mixed/transliterated coverage still deserves expanded review.

## 6. USER / ADMIN authentication

`SecurityConfig`, `AuthenticationService`, `AuthController` and V2 replace the static import token with database accounts and Spring Security.

- Passwords: BCrypt cost 12, minimum 12 characters, maximum 72 UTF-8 bytes.
- Sessions: random 256-bit opaque Bearer token, SHA-256 hash only in DB, default eight-hour expiry, maximum 24h, logout revocation.
- Five failed attempts lock the account for 15 minutes.
- ADMIN provisions USER/ADMIN accounts; no public registration.
- Disabling an account revokes all its tokens; administrators cannot disable themselves.
- Authenticated USER/ADMIN may ask/read rules/documents; ADMIN alone imports rules and manages users/amendments.
- Login and health are public; no cookie/Basic authentication is used.

New auth routes: `POST /api/v1/auth/login`, `GET /api/v1/auth/me`, `POST /api/v1/auth/logout`, `POST /api/v1/admin/users`, `POST /api/v1/admin/users/{id}/disable`.

First startup: set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `BVS_BOOTSTRAP_ADMIN_USERNAME` and a unique long `BVS_BOOTSTRAP_ADMIN_PASSWORD`. Remove bootstrap variables after provisioning. Create a second ADMIN for maker/checker review. All cross-platform run/login examples are in [backend README](../backend/README.md).

Frontend follow-up: add login, authenticated fetch and a PDF Blob viewer; plain PDF links cannot supply Bearer headers. Do not put access tokens in URLs. Internal AI token is distinct from user access tokens.

The database-password fallback was removed. Any previously shared/committed password still needs external rotation. HTTPS, private networking, MFA/SSO, account recovery, distributed rate limiting and deployment monitoring are not implied by this implementation.

## Verification evidence

| Suite | Result | What was exercised |
|---|---|---|
| `cd backend && ./mvnw test` | 34 passed; zero failed/errors/skips | Unit/controller/contracts, 17 actual sample responses, five real PostgreSQL 16 integration tests. |
| `cd ai-service && python3 -m unittest discover -s tests -v` | 40 passed | HTTP/token contracts, fake provider, intent/facts/units, numeric tables and fail-closed semantic review. |
| `git diff --check` | Clean | Whitespace/patch sanity. |

Docker integration used disposable containers, not the configured hosted database. No live provider key was configured, so no paid generation/verification quality benchmark was run. Official response tests start Python automatically; `-Dbvs.test.python=/path/to/python` overrides executable discovery. Windows uses `python`, macOS/Linux `python3` by default.

## Acceptance still requiring external input

1. Obtain, verify, ingest and independently approve the actual August 2026 notification and any amended numerical definitions.
2. Domain reviewer signs off on table interpretation, complete response golds, Hindi terminology and legal qualifiers.
3. Run a larger live-model adversarial/bilingual evaluation and measure latency/cost of generation plus separate verification.
4. Build frontend authentication and clerk/admin workflows; conduct deployment security/load/backup checks.

The requested backend/AI mechanisms are implemented and regression-tested. These remaining inputs prevent claiming the overall project is perfect, legally certified or production-ready.

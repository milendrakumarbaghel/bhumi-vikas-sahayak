# Testing and Delivery Plan

## 1. Test strategy by layer

### React + TypeScript

Add Vitest, React Testing Library, `@testing-library/user-event`, `jest-axe` (or equivalent) and Playwright.

Required tests:

- Question form validation, Unicode Hindi entry, language override and normalized fact fields.
- Rendering of every `AnswerResponse.status` discriminated-union branch.
- Calculation breakdown and citation-card fields/links.
- Loading, timeout, retry, 429, validation and provider-unavailable states.
- Safe rendering of source excerpts (no injected HTML/script).
- Keyboard-only flow, focus restoration, live-region announcement and color-independent status.
- Print/copy output includes answer, citations, as-of date, version and disclaimer.
- Responsive checks at 360 px and standard desktop widths.
- Contract fixtures generated/validated against OpenAPI.

### Spring Boot unit tests

- Intent/language/fact normalization without external providers.
- Each calculator, using parameterized boundary tests and `BigDecimal` comparisons.
- Effective-date resolution including gaps, overlap rejection and supersession.
- Context construction limits and instruction isolation.
- Citation validation: invalid ID, excerpt mismatch, ineffective provision, missing claim evidence and numeric mismatch.
- Status decision logic for not found, ambiguity, missing facts and provider failure.
- Log/query redaction.
- Backend-to-AI-service contract parsing, unknown-topic tolerance, measurement re-verification and deterministic fallback on timeout/unavailability.

### Separate AI-service tests

- Provider configuration absent, provider timeout/rate limit/refusal and malformed structured output.
- Internal-token authorization and no public CORS exposure.
- Prompt/schema versioning and model metadata.
- Hallucinated measurements are removed before response; the backend independently repeats this validation.
- Deterministic fake provider tests require no network or paid API.

### Spring MVC/API tests

Use MockMvc/WebTestClient-style tests appropriate to the selected MVC stack:

- JSON validation and `ProblemDetail` shapes.
- Language/as-of metadata and all semantic status responses.
- CORS/security rules, unauthorized admin APIs, request-size limits and rate-limit behavior.
- Source endpoint path/page validation and safe headers.
- AI provider stub: success, malformed structured output, timeout, 429 and unavailable provider.
- OpenAPI contract validation.

### Persistence/integration tests

Use Testcontainers PostgreSQL matching production, with pgvector enabled when selected.

- Flyway migrates an empty database and upgrades the prior released schema.
- JSONL import produces expected counts (106 top-level records), unique keys and valid page ranges.
- Re-running identical ingestion is idempotent.
- Embedded rules are addressable and retain parent/source lineage.
- Full-text/vector indexes return expected rules for English, Hindi and mixed queries.
- Active corpus switch is atomic and rollback works.
- Answer/citation/calculation traces reproduce a response.
- Runtime role cannot perform migration/admin-only actions.

### End-to-end tests

Run React + Spring Boot + test PostgreSQL with a deterministic fake AI provider for CI:

- Ask English and Hindi questions through the browser.
- Complete a `NEEDS_INPUT` follow-up and receive a calculated answer.
- Open a citation at the expected source/page.
- Verify not-found UX for property tax/market rate.
- Verify amendment version banner and as-of behavior.
- Submit feedback.
- Simulate provider failure and ensure no uncited answer appears.

## 2. Golden evaluation dataset

Convert the 17 supplied questions into versioned cases with:

- stable case ID and language;
- question plus required facts;
- `asOfDate`;
- expected status;
- expected rule/sub-rule/page set;
- expected exact values/units and allowed explanatory variants;
- mandatory qualifiers/caveats;
- prohibited claims;
- reviewer and review date.

Expand with paraphrases and boundaries rather than scoring only the supplied wording.

Minimum cases:

| Category | Cases |
|---|---|
| Rule 56 front/rear setbacks | road widths 9, just over 9, 12, just over 12, 18, just over 18; plot areas 40, just over 40, 150, just over 150, 225, just over 225; >12.5 m height routes to Rule 42. |
| Rule 21 fees | every residential band boundary; commercial +50%; high-rise per-area; addition/alteration; revalidation; missing built-up area/use/high-rise status. |
| Rule 17 | exactly/under 0.40 ha, FAR calculation, road-width and height conditions; distinguish 17-a tourist facility. |
| Rule 23 | grant date, expiry, before/after-expiry fee condition, first/second revalidation, work commenced. |
| Rule 52 | low/medium, <=33000 V, >33000 V, and the stricter external Electricity Rules caveat. |
| Amendment | before, on and after verified effective date for Rule 2(5)(c); absence of authoritative amendment returns review status. |
| Hindi/parity | direct Hindi, code-mixed and transliterated queries; values/citations match equivalent English cases. |
| Not found | supplied property-tax and market-rate questions plus adversarial unrelated questions. |
| Citation robustness | tempting adjacent rule, invalid generated page, excerpt mismatch and prompt injection. |

## 3. Acceptance gates

### MVP gate

- All 17 supplied questions have reviewed expected outcomes.
- 100% citation validity and calculator boundary tests pass.
- Both supplied out-of-scope questions return `NOT_FOUND_IN_RULES` with no rule citation.
- Expected rule is in retrieval top 5 for every answerable golden case.
- No critical/high security finding; exposed database credential has been rotated.
- English and Hindi core flows pass accessibility and end-to-end tests.
- Current corpus cutoff/amendment warning is visible and correct.

### Pilot gate

- >=95% expected-rule top-1 accuracy on a larger expert-reviewed set; report sample size and confidence interval.
- >=95% fully correct answer score and 100% validated citation coverage on high-risk numerical cases.
- 100% amendment effective-date cases pass.
- P95 <=10 seconds for AI answers and <=1 second for calculator-only answers under pilot load.
- Backup/restore, rollback, monitoring alerts and runbook are exercised.
- Department/domain reviewer signs off on terminology, disclaimers and calculator tables.

Any lower achieved metric must be disclosed; do not present a small sample as general accuracy.

## 4. Current test gap

The backend currently has only `contextLoads()`. There are no product unit/integration tests and no frontend tests. Because the configured datasource is external, even the context test is not an isolated proof of application health. The target suite must not depend on the committed hosted database.

## 5. Prioritized implementation sequence

### Phase 0 - secure and stabilize foundation

1. Rotate the exposed database password and externalize secrets.
2. Add explicit Spring profiles, safe security/CORS behavior and Actuator.
3. Add Flyway; replace `ddl-auto=update` with controlled migration/validation.
4. Establish frontend/backend CI checks and Testcontainers.

Exit: clean startup with local test PostgreSQL; no secrets in code; migrations/tests deterministic.

### Phase 1 - corpus and cited retrieval

1. Implement source/corpus/provision/chunk schema and JSONL importer.
2. Normalize running headers, expose embedded rule identifiers and preserve raw text.
3. Implement PostgreSQL lexical search, then embeddings/hybrid ranking.
4. Implement source/rule endpoints and immutable citation excerpts.
5. Add golden retrieval cases.

Exit: API retrieves correct passages with page/rule metadata, without LLM generation.

### Phase 2 - clerk experience and grounded answers

1. Replace Vite scaffold with the bilingual single-screen workflow.
2. Implement typed answer API/statuses and provider abstraction.
3. Add structured grounded generation and citation validator.
4. Implement not-found/needs-input/needs-review behavior.
5. Add copy/print, source viewer and disclaimer/version banner.

Exit: English/Hindi questions produce concise, validated, cited answers or safe non-answers.

### Phase 3 - deterministic calculators

1. Review source tables against official PDF.
2. Implement Rule 56 and Rule 21 first, then Rules 17, 23, 52, 42/61.
3. Add explicit fact forms and calculation breakdown UI.
4. Run exhaustive band-boundary tests.

Exit: all demonstrated numeric answers come from reviewed code/table definitions, not model arithmetic.

### Phase 4 - amendments and operations

1. Obtain and verify the official August 2026 amendment.
2. Implement effective-date/supersession workflow and maker-checker activation.
3. Add answer traces, feedback, metrics, dashboards and rollback runbook.
4. Complete department review and pilot hardening.

Exit: current/as-of-date answers are reproducible, amendments are safely activated, and operators can diagnose failures.

## 6. Requirement traceability sample

| Requirement | Primary API/component | Required tests |
|---|---|---|
| FR-QA-001/002/003 | `QueryForm`, language service, answer endpoint | Hindi Unicode, auto/override, parity E2E |
| FR-SAFE-001 | orchestrator status policy | sample questions 16/17, adversarial OOD |
| FR-CITE-001-004 | citation validator/cards/source endpoint | excerpt match, claim coverage, invalid page/ID |
| FR-CALC-001-006 | calculation endpoint/services | exhaustive boundary/units/missing facts |
| FR-AMD-001-006 | corpus/amendment services | before/on/after date, approval, rollback |
| NFR-SEC-001/002 | configuration/security/filtering | secret scan, CORS/auth/rate/HTML injection |
| NFR-A11Y-001 | React core flow | automated axe plus manual keyboard/screen reader |

## 7. Definition of done for each requirement

A requirement is not complete until:

1. Implementation and database migration (if needed) are reviewed.
2. Unit/integration/UI tests exist and pass.
3. OpenAPI and user/operator documentation are updated.
4. Hindi/English behavior is considered, not deferred implicitly.
5. Security/privacy logging implications are reviewed.
6. The feature is linked to one or more requirement IDs and acceptance evidence.
7. No known limitation is hidden; it appears in the product warning or README as appropriate.

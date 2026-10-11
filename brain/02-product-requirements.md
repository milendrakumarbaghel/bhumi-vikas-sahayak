# Product Requirements

## 1. Purpose and success definition

Bhumi Vikas Sahayak shall let a Bhopal Municipal Corporation building-permission clerk ask a building-rule question in Hindi or English and receive, within the counter interaction, a concise answer in the same language with verifiable source text, rule/sub-rule and PDF page references.

Success means the clerk can answer common questions in under one minute, numerical answers match reviewed rule tables, silent/out-of-scope cases are identified as not found, and every legal claim is traceable to an effective source version.

## 2. Primary user and workflow

Primary user: a BMC building-permission clerk serving a citizen. Secondary validation users may include municipal reviewers and architects, but their workflows must not expand the first release into a general portal.

Core workflow:

1. Clerk selects or automatically uses Hindi/English and enters a natural-language question.
2. The system detects required facts (for example plot area, building height, road width, use and effective date).
3. If a fact is essential, the UI asks a focused follow-up instead of assuming it.
4. The backend retrieves only applicable, effective rule passages and invokes deterministic calculators for numeric conclusions.
5. The answer presents a short conclusion, assumptions/inputs, calculation breakdown where applicable, citations and exact source excerpts.
6. The clerk can open the cited official PDF page and can copy/print the answer with its disclaimer and source version.

## 3. Functional requirements

### Query and answer

| ID | Requirement | Acceptance condition | Current status |
|---|---|---|---:|
| FR-QA-001 | The React UI shall accept Unicode questions in Hindi (Devanagari), English or common mixed-language input. | Pasting and submitting each form works without corruption; request encoding is UTF-8. | Missing |
| FR-QA-002 | The UI shall offer explicit Hindi and English selection plus an automatic mode. | Selected/detected language is returned in API metadata and can be corrected before resubmission. | Missing |
| FR-QA-003 | The Spring service shall return the answer in the user's selected/detected language while keeping rule identifiers unchanged. | Hindi input produces natural Hindi; English input produces English; citations remain canonical. | Missing |
| FR-QA-004 | Answers shall be concise by default and structured as conclusion, conditions/assumptions, calculation (if any), citations and disclaimer. | A common counter query can be read in under one minute. | Missing |
| FR-QA-005 | The system shall ask for missing decisive facts and shall not fabricate defaults. | A setback question without building height/use/road width requests only facts needed to choose a rule/table row. | Missing |
| FR-QA-006 | The system shall support multi-rule answers and identify conflicts or precedence. | A result can cite multiple provisions and explain Rule 103 precedence where applicable. | Missing |
| FR-QA-007 | Each response shall include a stable answer ID and trace metadata for support/evaluation, without storing personal data. | Backend logs and response share a correlation ID; query retention follows policy. | Missing |
| FR-QA-008 | The system shall not present itself as legal advice or an approval decision. | Disclaimer is visible on every answer and exported copy. | Missing |

### Not-found, ambiguity and safety behavior

| ID | Requirement | Acceptance condition | Current status |
|---|---|---|---:|
| FR-SAFE-001 | If retrieved evidence does not support the answer, return `NOT_FOUND_IN_RULES`; do not let the model fill the gap. | Property-tax and current land-rate sample questions return not found with no invented rule. | Missing |
| FR-SAFE-002 | If two effective provisions appear inconsistent or the question depends on external rules/data, return `NEEDS_REVIEW` and state why. | Answer separates known text from unresolved dependency and lists relevant citations. | Missing |
| FR-SAFE-003 | Prompt injection or instructions contained in source text/user text shall not override system grounding policy. | Red-team tests cannot suppress citations, expose secrets or request unrelated actions. | Missing |
| FR-SAFE-004 | The system shall distinguish no answer, low confidence, ambiguous question and unavailable service. | API uses different machine-readable status/error codes; UI gives an appropriate recovery action. | Missing |

### Source citations

| ID | Requirement | Acceptance condition | Current status |
|---|---|---|---:|
| FR-CITE-001 | Every substantive rule claim shall cite rule, sub-rule/table when identifiable, title, PDF page(s), exact supporting excerpt, source document and consolidation/effective date. | Citation cards contain all fields and excerpt is found in the stored source chunk. | Partial: source metadata only |
| FR-CITE-002 | A citation link shall open the repository-served official PDF at the relevant page or an in-app source viewer with that page identified. | User reaches the cited page in at most one action. | Missing |
| FR-CITE-003 | Citation excerpts shall be copied from immutable source text, never generated or translated by the LLM. | Automated verifier rejects excerpts not byte/normalized-text matched to source. | Missing |
| FR-CITE-004 | Generated sentences shall be mapped to one or more citation IDs. | Unsupported claim detection fails the answer closed or marks it for review. | Missing |
| FR-CITE-005 | Hindi answers shall show a Hindi explanation but retain a canonical English citation and, when curated/verified, the corresponding official Hindi page/excerpt. | UI never labels machine translation as official Hindi text. | Missing |

### Deterministic numerical results

| ID | Requirement | Acceptance condition | Current status |
|---|---|---|---:|
| FR-CALC-001 | Fees, setbacks/open spaces, FAR/area, building-height bands, electric-line clearances and sanction duration shall be computed by versioned Java domain calculators, not by the LLM. | Identical inputs and source version always yield identical value and rule-row citation. | Missing |
| FR-CALC-002 | The LLM may extract proposed inputs, but the backend shall validate types, units, ranges and required context before calculation. | `10 m`, `10 metres`, `10 मीटर` normalize to meters; invalid/negative values are rejected. | Missing |
| FR-CALC-003 | Results shall show normalized inputs, formula/table band, inclusivity boundaries, result, units and rounding policy. | A 10 m road maps to the `>9 and <=12 m` band and returns 3.6 m under Rule 56 for an eligible residential building <=12.5 m. | Missing |
| FR-CALC-004 | Fee answers shall separate application fee, permission fee and other charges; they shall not collapse distinct legal fees. | A residential building query names which Rule 21 fee is being calculated and what input is still needed. | Missing |
| FR-CALC-005 | Calculator rule tables shall be human-reviewed against the official PDF before activation. | Each active table row records reviewer, review date, source version, rule/table/page and checksum. | Missing |
| FR-CALC-006 | When inputs fall outside modeled rows or depend on another authority/rule, the calculator shall return `NEEDS_REVIEW`, not extrapolate. | No arithmetic or table extrapolation occurs. | Missing |

Initial calculators, in delivery order:

1. Rule 56 residential front/rear/side open space, including exact boundary cases.
2. Rule 21 application and permission fees by use, high-rise status, built-up area and alteration/revalidation case.
3. Rule 17 farmhouse limits and derived maximum floor area (`plot area x FAR`) while keeping hectares/square meters explicit.
4. Rule 23 validity/revalidation timeline.
5. Rule 52 electrical clearances, including the "whichever is more" caveat.
6. Rule 42 high-rise norms and Rule 61 FAR only after their tables are fully reviewed.

### Amendment and effective-date behavior

| ID | Requirement | Acceptance condition | Current status |
|---|---|---|---:|
| FR-AMD-001 | Each source and provision shall have publication, effective, superseded and ingestion dates plus authority and provenance. | Queries can resolve the rule effective on a supplied `asOfDate`. | Missing |
| FR-AMD-002 | The default answer date shall be today's date in Asia/Kolkata and shall be displayed. | Responses state “Rules considered as of …”; client-provided dates are validated. | Missing |
| FR-AMD-003 | The 28 August 2026 amendment to Rule 2(5)(c) shall be ingested separately from the March 2025 consolidation and linked as a superseding provision after authoritative verification. | A 300 sq m Development Plan question uses the post-amendment provision for dates on/after its effective date and the older provision before it. | Missing |
| FR-AMD-004 | New amendments shall enter draft/reviewed/active/retired states and require maker-checker approval before affecting answers. | Draft content is queryable only in admin tests, never public answers. | Missing |
| FR-AMD-005 | The UI shall warn when the corpus has a known cutoff or unverified later amendment. | March 2025-only answers do not claim to be current through 2026. | Missing |
| FR-AMD-006 | Re-indexing shall be reproducible and rollbackable by corpus version. | Previous active version can be restored without deleting audit history. | Missing |

### User interface

| ID | Requirement | Acceptance condition | Current status |
|---|---|---|---:|
| FR-UI-001 | Replace the Vite starter with a focused single-workflow screen: language, question, submit, examples and answer. | No framework/demo content remains in product UI. | Missing |
| FR-UI-002 | Render answer statuses distinctly: answered, needs more information, not found, needs review and error. | Status is conveyed by text/icon as well as color. | Missing |
| FR-UI-003 | Citation cards shall be keyboard accessible, expandable and printable. | WCAG keyboard/focus checks pass; source excerpt remains legible on mobile. | Missing |
| FR-UI-004 | UI shall work at a municipal desktop and on a narrow mobile/tablet screen. | No horizontal overflow at 360 CSS px; main action remains visible. | Missing |
| FR-UI-005 | Loading shall show progress and allow retry without duplicate submissions. | Button is safely disabled/deduplicated; request ID prevents accidental duplicate work. | Missing |
| FR-UI-006 | Example questions shall include Hindi, English, numerical and not-found examples drawn from the supplied set. | Each example populates the question and remains editable. | Missing |
| FR-UI-007 | Users shall be able to copy or print the complete answer with citations, version date and disclaimer. | Export contains no hidden chain-of-thought or internal scores. | Missing |

### Administration and observability

| ID | Requirement | Acceptance condition | Current status |
|---|---|---|---:|
| FR-OPS-001 | Provide an authenticated corpus ingestion/activation workflow or an offline administrator command using the same services. | Only authorized operators can activate a corpus version. | Missing |
| FR-OPS-002 | Record retrieval candidates, selected chunk IDs, calculator calls, latency, model/provider version, answer status and citation-validation result. | A support user can reproduce an answer without storing personal details. | Missing |
| FR-OPS-003 | Collect explicit helpful/not-helpful feedback with optional non-personal notes. | Feedback links to answer ID, not a citizen profile. | Missing |
| FR-OPS-004 | Expose health/readiness endpoints that distinguish app, database and AI-provider readiness. | Orchestrator can avoid routing traffic when required dependencies are unavailable. | Missing |

## 4. Non-functional requirements

| ID | Requirement / target |
|---|---|
| NFR-PERF-001 | P95 answer latency <=10 seconds with AI and <=1 second for calculator-only responses under the agreed pilot load. The UI must acknowledge submission within 200 ms. |
| NFR-REL-001 | A provider timeout shall yield a retryable service response; it must never produce an uncited fallback answer. |
| NFR-ACC-001 | 100% of displayed rule claims must have validated citations; 100% of calculator boundary tests must pass; the two supplied out-of-domain cases must return not found. |
| NFR-SEC-001 | No credentials in source, client bundles, logs or API payloads. Configuration is environment-based and credentials are rotated. |
| NFR-SEC-002 | Validate request size/content, escape rendered source text, use parameterized persistence, restrict CORS, rate-limit public queries and redact logs. |
| NFR-PRIV-001 | Do not solicit/store names, phone numbers, property identifiers or submitted plans. Display a “do not enter personal data” notice. Define query/log retention. |
| NFR-A11Y-001 | Meet WCAG 2.2 AA for the core query-answer flow, including Hindi text, focus order, contrast, labels and live announcements. |
| NFR-I18N-001 | All UI strings live in typed locale resources; no Hindi/English strings are embedded across components. Use locale-aware number formatting while preserving legal units. |
| NFR-MAINT-001 | Controllers contain no business logic; calculators and retrieval policies have independent interfaces and tests. Database changes use Flyway. |
| NFR-AUDIT-001 | Active source/provision/calculator versions and approvals are immutable and auditable. No hard deletion of a version that was used in an answer. |
| NFR-LEGAL-001 | Each response states source cutoff/as-of date and that the tool is informational support, not legal advice or approval. |

## 5. Out of scope for the first release

- Login, scraping or writes to government portals.
- Building-plan upload/OCR or automated plan approval.
- Property tax, land valuation, market rates or unrelated municipal services.
- Citizen profiles, payment collection, application submission or case tracking.
- Unreviewed answers from general web search.
- Treating machine-translated Hindi as official gazette text.

## 6. Minimum acceptance demo

The release candidate must demonstrate:

1. English Rule 56 query: 10 m road, 9 m residential building -> 3.6 m front open space with source excerpt/page.
2. Hindi Rule 56 query: 200 sq m plot and 9 m road -> explain that front space depends on road width (3.0 m at exactly 9 m) and distinguish plot-area-dependent rear space.
3. Rule 17 farmhouse query -> 0.40 hectare minimum and FAR 0.10, with optional derived area only when plot size is provided.
4. Rule 23 query -> three years plus two consecutive one-year revalidations, subject to execution/revalidation conditions.
5. Rule 52 query -> 2.5 m vertical and 1.2 m horizontal for low/medium voltage, with applicable-Electricity-Rules caveat.
6. Rule 21 fee query -> asks for built-up area/use/high-rise status when missing and shows a deterministic breakdown when supplied.
7. August 2026 amendment query -> effective-date-aware response and version banner.
8. Property-tax and Kolar Road market-rate questions -> `NOT_FOUND_IN_RULES`, no invented citation.

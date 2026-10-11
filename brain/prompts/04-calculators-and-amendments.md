# Prompt: Deterministic Calculators and Amendment Handling

Work in `/Users/milendrakumarbaghel/Documents/MPSeDC/bhumi-vikas-sahayak`.

Implement deterministic legal calculations and effective-dated amendment handling for Bhumi Vikas Sahayak. Read all `brain/` documents and inspect existing schema/backend interfaces first. Preserve unrelated work.

Calculators:

- Implement typed Java calculators using `BigDecimal`, explicit units/enums and `LocalDate asOfDate`.
- Priority order: Rule 56 front/rear/side open space; Rule 21 fees; Rule 17 farmhouse limits/derived floor area; Rule 23 duration/revalidation; Rule 52 electrical clearance; then Rules 42/61 only after their tables are fully reviewed.
- Store/refer to reviewed, versioned calculation definitions/bands and return normalized inputs, matched band/formula, result, warnings and exact citations.
- Model inclusive/exclusive boundaries explicitly. Add exhaustive boundary tests, including 9/12/18 m road widths and 40/150/225 sq m plot areas.
- Keep application fee and permission fee distinct. Do not infer built-up area, use, high-rise status or external Electricity Rules.
- Return `NEEDS_INPUT` for missing decisive facts and `NEEDS_REVIEW` for unmodeled/external-rule cases. Never extrapolate.

Amendments:

- Implement immutable source versions, provision relations and effective-date resolution with no overlaps/gaps for the same canonical provision.
- Implement draft -> reviewed -> active -> retired lifecycle with maker-checker separation and atomic activation/rollback.
- Keep the 6 March 2025 consolidation immutable.
- The repository mentions a 28 August 2026 Rule 2(5)(c) amendment but does not contain its authoritative official text. Do not activate secondary-summary wording. Add the model/workflow/tests/placeholder metadata needed to ingest it once an official gazette is supplied.
- Until verified, affected current-date queries must produce `NEEDS_REVIEW` plus a corpus-cutoff warning.
- Add before/on/after-date tests for a fixture amendment and a pending-verification test for the real 2026 item.

Verification:

- Compare every activated numeric row against the official English PDF pages and record source page/checksum/reviewer metadata. If visual verification cannot be completed, leave that definition non-active and document it.
- Add pure unit tests, persistence tests and API contract tests. Ensure calculator-only responses work with the AI provider disabled.

Deliverables:

- Calculation/effective-date code, data/migrations where required, tests and review fixtures.
- A calculator coverage matrix showing active, pending review and unsupported cases.
- Exact commands run and explicit items requiring a human/legal reviewer.

Do not let an LLM perform final arithmetic or choose legal table rows.

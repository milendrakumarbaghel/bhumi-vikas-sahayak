# Prompt: Full Integration and Release Review

Work in `/Users/milendrakumarbaghel/Documents/MPSeDC/bhumi-vikas-sahayak`.

Act as the final integration owner for Track 1 - Bhumi Vikas Sahayak. Read every file under `brain/`, inspect all frontend/backend/database/RAG/calculator/test changes and preserve unrelated user work. Your job is to close integration gaps and produce an evidence-based release assessment, not to rewrite working components.

Verify end to end:

1. React request/response types match the live Spring OpenAPI contract.
2. Spring starts against a clean migrated local/test PostgreSQL database without external hosted credentials.
3. The 106-record corpus imports idempotently and citations open the correct source/page.
4. English, Hindi and code-mixed queries retrieve effective provisions.
5. Every answered legal claim has validated citations and exact source excerpts.
6. Numerical answers use versioned Java calculators and expose inputs/bands/units.
7. Missing facts return focused follow-ups; unsupported/external questions return safe statuses.
8. The March 2025 cutoff and unverified August 2026 amendment are handled honestly.
9. The two supplied out-of-domain questions never receive invented rules.
10. Security, privacy, accessibility, performance and operational acceptance gates are tested.

Run the full repository checks and the golden evaluation. Fix only issues clearly within the documented architecture and requirements. If a legal table/amendment needs human verification, leave it inactive and record a blocker rather than guessing.

Required release artifacts:

- Root README updated with team placeholders, one user/workflow, architecture, capabilities/limitations, <=5-step local run instructions, data sources and safe configuration.
- License status called out; add a license only if the user/project authority has selected it.
- OpenAPI artifact, test/evaluation report and calculator/corpus version matrix.
- No secrets or generated build junk committed.
- A concise demo script covering English, Hindi, calculation, citation, amendment warning and not-found behavior.

Final report must classify each requirement as completed, partial, missing or blocked with evidence (file/test/API), list all commands run, disclose unrun checks, and state whether the MVP gate is met. Do not claim release readiness if any critical citation, calculation, secret or amendment gate fails.

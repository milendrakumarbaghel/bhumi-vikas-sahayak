# Prompt: React + TypeScript Frontend

Work in `/Users/milendrakumarbaghel/Documents/MPSeDC/bhumi-vikas-sahayak`.

Replace the current Vite starter screen with the focused Bhumi Vikas Sahayak clerk workflow. Read all documents under `brain/`, especially the product requirements and API contract. Inspect the live backend OpenAPI contract before coding; preserve unrelated work.

Use the existing React 19 + TypeScript + Vite project. Do not switch frameworks and do not introduce a backend-in-the-frontend.

Build:

- A professional, simple single-workflow page for a municipal clerk: product identity, corpus/as-of banner, language control (`Auto`, `English`, `हिंदी`), question area, editable examples, submit action and result region.
- Typed API client/models generated from or checked against backend OpenAPI. Represent `ANSWERED`, `NEEDS_INPUT`, `NOT_FOUND_IN_RULES`, `NEEDS_REVIEW` and error states as a discriminated union with exhaustive rendering.
- Follow-up fact controls for measurements/enums/booleans returned by `requiredFacts`, while retaining the original question.
- Answer card with short conclusion, assumptions, warnings, deterministic calculation breakdown and disclaimer.
- Accessible expandable citation cards showing rule/sub-rule, title, part, exact excerpt, PDF pages, source language/version and an open-source action.
- Copy and print views containing answer, calculations, citations, source date/version and disclaimer; never include internal prompts/scores.
- Loading, cancel/retry, rate-limit, validation, timeout and offline/provider-unavailable UX. Prevent accidental duplicate submissions.
- Responsive layout down to 360 CSS px, visible keyboard focus, non-color-only statuses and screen-reader announcements.
- Typed locale dictionaries for all UI strings; Hindi/English must not be scattered as component literals. Use fonts/fallbacks that render Devanagari cleanly.
- A visible notice not to enter personal information and that the tool is informational support, not approval/legal advice.

Design constraints:

- Optimize for a correct answer in under one minute, not for a multi-page government portal.
- Do not show a confidence percentage as legal certainty.
- Do not fabricate placeholder legal values in production UI.
- Preserve exact citation text returned by the backend; never translate it silently.
- If backend endpoints are incomplete, use local typed fixtures behind a development-only adapter and clearly mark them; do not hardcode fake responses into production flow.

Tests:

- Vitest + React Testing Library/user-event for all states and interactions.
- Accessibility assertions plus manual keyboard checklist.
- Playwright for English, Hindi, `NEEDS_INPUT`, citation opening, not-found, provider failure and copy/print flows.
- Responsive visual/screenshot checks where practical.
- Run lint, type-check, unit tests and production build.

Deliverables:

- Frontend code/tests and concise run instructions.
- Screenshots or a brief UI-state inventory.
- Requirement-ID mapping and commands run.

Do not modify backend/domain logic unless a minimal contract fix is essential; report such changes explicitly.

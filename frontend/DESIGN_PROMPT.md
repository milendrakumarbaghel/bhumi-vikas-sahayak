# Build the frontend for "Bhumi Vikas Sahayak"

> Copy-ready prompt for building the clerk's screen. The API section was checked against
> `backend/src` on 11 Oct 2026. If the backend changes, re-check the API section first.

## Who it is for
ONE user: a building-permission clerk at the Bhopal Municipal Corporation counter. A citizen asks
e.g. "My plot is 200 sq m on a 9 m road. How much gap at the front, how many floors, what fee?"
The clerk types it in English or Hindi and needs, within a minute, a short answer in the same
language with the rule number and the exact rule text. The tool points to the rule; the clerk
decides. It is NOT legal advice. Optimise for speed and trust, not decoration.

## Stack and starting point
- Existing app in `frontend/`: React 19, Vite 8, TypeScript 6, Tailwind CSS 4, lucide-react.
- First read `frontend/src` (App.tsx, components/, services/, types.ts, data/) and EXTEND it; do
  not start over. Keep components small and readable (the code is handed to MPSeDC).
- API base URL from `import.meta.env.VITE_API_BASE_URL` (default `http://localhost:8080`).
  Add `frontend/.env.example`. The browser calls ONLY the Java backend, never the ai-service.
- No login for this screen. Do not build admin pages (amendments, users): out of scope.
- Do not store questions or answers in localStorage/cookies (may contain citizen details).

## API (verified against backend/src on 11 Oct 2026)
POST `/api/v1/questions:answer`

Request: `{ "question": string (3-2000 chars), "language": "auto" | "en" | "hi", "asOfDate"?: "YYYY-MM-DD" }`

Response 200:
```ts
type Status = "ANSWERED" | "NEEDS_MORE_INFORMATION" | "NEEDS_REVIEW" | "NOT_FOUND_IN_RULES";
interface AskResponse {
  answerId: string; status: Status; language: "en" | "hi"; asOfDate: string;
  sourceVersion: string;            // e.g. "MP Bhumi Vikas Rules, 2012; consolidated through 6 March 2025"
  sourceDocumentUrl: string;        // "/api/v1/documents/rules.pdf" (relative to API base)
  answer: string; citations: Citation[]; calculation: CalculationPayload;
  warnings: string[]; disclaimer: string;
}
interface Citation {
  ruleNumber: string; title: string; passageLabel: string;   // passageLabel e.g. "56(1)", "17-a"
  pageStart: number; pageEnd: number; sourceUrl: string; excerpt: string; validated: boolean;
}
// calculation arrives in ONE of three shapes - handle all:
// (a) {} -> no calculation
// (b) { calculations: Calc[], missingInputs: string[], notApplicable: string[] }   (ai-service)
// (c) { code, rule, matchedBand, roadWidthMeters, frontOpenSpaceMeters }          (Java fallback)
interface Calc {
  code: string; rule: string; pdfPage: number; matchedBand: string; formula: string;
  inputs: { name: string; value: number | string | boolean; unit: string }[];
  result: { name: string; value: number; unit: string; text?: string;
            floorIllustration?: { groundCoveragePercent: number; footprintSqm: number; floors: number }[];
            floorIllustrationLabel?: string; [k: string]: unknown };
  assumptions: string[]; notes: string[];
}
```
Errors: 422 `application/problem+json` with `errors[]` (field, message); 429 too many requests;
5xx / network failure -> service unavailable.

GET `/api/v1/rules/{ruleNumber}` -> `{ ruleNumber, title, part, pageStart, pageEnd, text, passages[{label, pageStart, pageEnd, text}] }`

PDF: `${API}/api/v1/documents/rules.pdf#page={pageStart}` (open in a new tab).

## One screen, top to bottom
1. Header: "Bhumi Vikas Sahayak / भूमि विकास सहायक", subtitle "MP Bhumi Vikas Rules 2012".
2. Question box: large multiline textarea (min 3 rows, 18px+ text), placeholder in both languages.
   Enter submits, Shift+Enter adds a new line. Character counter near 2000.
   Language toggle: Auto | English | हिन्दी (default Auto). Big "Ask / पूछें" button.
3. Five example chips (click fills AND submits), exact text:
   - मेरा प्लॉट 200 वर्ग मीटर का है और सामने 9 मीटर चौड़ी सड़क है। आगे कितनी जगह खाली छोड़नी होगी?
   - My house faces a 10 m wide street and will be 9 m tall. How much open space do I leave at the front?
   - What is the fee for building permission for a residential house?
   - I want a farm house on agricultural land. What is the minimum plot size and the maximum FAR?
   - How do I pay my property tax online?
4. Answer card. Short answer FIRST, in large text, with `lang` set to the response language.
   Distinct state per status (colour + icon + heading, never colour alone):
   - ANSWERED: green, check icon, "Answer from the rules / नियमों से उत्तर".
   - NOT_FOUND_IN_RULES: amber, "Not found in the rules. Check with the department. /
     नियमों में नहीं मिला। विभाग से पुष्टि करें।" No citations shown.
   - NEEDS_MORE_INFORMATION: blue, "Need more information". Read the missing inputs from
     `calculation.missingInputs` and from warnings starting with "Required inputs:". Show an
     inline mini-form (number + unit select: sq m / sq ft / gaj for areas; m / ft for lengths;
     a select for building use). On submit, append the facts to the original question in the
     same language (e.g. " Road width: 9 m.") and ask again.
   - NEEDS_REVIEW: orange/red, "Needs officer review" - e.g. an amendment applies. Show the
     answer and citations, with the review reason from warnings at the top.
5. Calculation panel (only when there is one): for each calc show the result big
   (use `result.text` if present, e.g. "Rs. 3,000"), then rule + PDF page link, matched band,
   formula, inputs table, assumptions (italic, "Assumption"), notes. If `floorIllustration`
   exists, show it in a separate grey box headed with `floorIllustrationLabel`
   ("ILLUSTRATION ONLY, NOT FROM THE RULES"); never mix it with rule results.
   Show `notApplicable` reasons as plain text. Handle shape (c) as a single calc row.
6. Citation chips: "Rule 56(1) · p.77" (p.36-37 for ranges). Click expands:
   - the exact excerpt in a quote block, plus the full passage/rule text fetched from
     GET /api/v1/rules/{ruleNumber}, with the excerpt HIGHLIGHTED inside it (match after
     collapsing whitespace; if no match, show the excerpt alone);
   - "Open PDF page" link;
   - a "Verified against rule text" badge when `validated` is true. If `validated` is false,
     do NOT show the excerpt; show "Quote could not be verified - open the PDF page" instead.
7. Warnings: show every warning above the citations. If a warning mentions "2(5)(c)" or
   "amendment", show a prominent amber banner: "Rule 2(5)(c) was amended on 28 Aug 2026 (plots
   up to 500 sq m). That change is not in the loaded rules - check the latest notification." with
   link https://teamleaseregtech.com/updates/article/59469/government-of-madhya-pradesh-amendment-to-land-development-rules
8. Copy button: copies answer + "Rule X(y), p.N: "excerpt"" lines + calculation results +
   sourceVersion + disclaimer, as plain text the clerk can show or WhatsApp to the citizen.
   Show "Copied" feedback.
9. Footer, ALWAYS visible (sticky on mobile): `sourceVersion` (before the first answer show
   "MP Bhumi Vikas Rules 2012, consolidated to 6 March 2025") and "Points to the rule. The clerk
   decides. Not legal advice. / यह नियम की ओर संकेत करता है। निर्णय क्लर्क का है। यह कानूनी सलाह नहीं है।"

## Loading, errors, edge cases
- Loading: skeleton of the answer card + "Searching the rules... / नियम खोजे जा रहे हैं..."
  (answers can take 10-40 s). Disable the button; allow cancel (AbortController).
- 422: show field messages under the textarea. 429: "Too many questions, wait a minute".
  5xx/network: "Service unavailable, try again" with a Retry button. Never show stack traces.
- Empty `citations` on ANSWERED: show the answer with a "No verified citation" warning.

## Design rules
- Mobile-first, works at 360 px wide and on a counter desktop. Base font 18px, answer 20-22px,
  touch targets >= 44 px, WCAG AA contrast, visible focus rings, fully keyboard usable.
- Devanagari: load "Noto Sans Devanagari" (with system fallback) and set `lang="hi"` on Hindi
  text; slightly larger line-height for Hindi.
- Calm, official look: white/very light grey background, one accent colour (deep blue), status
  colours only for status. No animations beyond a subtle skeleton pulse. No stock images.
- All UI strings in one `i18n.ts` object with `en` and `hi`; UI language follows the toggle
  (Auto -> follow the last answer's language).

## Done when
- `npm run build` and `npm run lint` pass with no TypeScript errors.
- All five example chips produce the right state against the running backend
  (property tax -> NOT_FOUND_IN_RULES amber; Hindi question -> Hindi answer).
- Every status, the calculation panel (both shapes), citation expand + highlight, PDF link,
  copy, loading, 422/429/503 states have been checked in the browser at 360 px and 1280 px.
- Update `frontend/README.md`: how to run (`npm install`, `npm run dev`, set VITE_API_BASE_URL).

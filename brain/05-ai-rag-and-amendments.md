# AI, RAG and Amendment Requirements

## 0. Deployment boundary

All LLM provider clients, model credentials, prompts, embeddings and model-output parsing live in the separate top-level `ai-service/`. The main `backend/` contains only a narrow internal HTTP client plus deterministic verification/fallback logic. The frontend calls only the backend.

The backend remains the authority for corpus versions, retrieval acceptance, legal applicability, calculations, citations and final answer status. Output from `ai-service/` is always an untrusted proposal that must be validated.

## 1. Grounding principles

The system is evidence-first. Retrieval supplies candidate law, deterministic services supply numeric conclusions, and the language model explains only supported results. Fluency is never treated as evidence.

Hard rules:

1. Through the internal service contract, the generation model receives only the user question, normalized facts, effective retrieved chunks, calculator result and response schema.
2. The model must reference provided chunk IDs for every legal claim.
3. Exact excerpts come from PostgreSQL/source text after generation; the model cannot author them.
4. A post-generation validator rejects unknown citations, ineffective provisions, excerpt mismatches, unsupported claims and calculator discrepancies.
5. The backend rejects invalid AI-service output, optionally requests one bounded retry, or converts it to `NEEDS_REVIEW`/`NOT_FOUND_IN_RULES`; it is never shown as a confident answer.
6. Prompts and model output must not contain chain-of-thought in the API or logs. Store concise decision metadata only.

## 2. Retrieval design

### Chunking

- Chunk by legal structure, not fixed size alone: rule -> inserted rule -> sub-rule -> table/note/footnote.
- Repeat canonical rule/title/part and effective dates as metadata, not as generated text.
- Keep table headings and row labels with every table-row chunk.
- Target chunks small enough for precise citations but include neighboring context or parent summary for applicability.
- Keep raw and normalized text. Normalization may remove running headers/whitespace but not change numbers or legal wording.

### Hybrid search

Combine:

- exact rule-number/sub-rule lookup;
- PostgreSQL full-text and trigram matching for titles, legal terms and Hindi/English aliases;
- vector similarity over multilingual embeddings or translated retrieval queries;
- metadata filters for active corpus, effective date and language;
- reranking that rewards exact rule/table/number matches.

Candidate scores must be logged for evaluation but not presented as legal “confidence percentages.” A retrieval threshold is necessary but insufficient: the final answer must also pass evidence validation.

### Query understanding

Extract typed facts such as occupancy, plot area, built-up area, road width, building height, frontage, voltage class, dates and requested action. Preserve original values and normalization. If the user's explicit fact conflicts with extracted prose, ask for confirmation.

Intent classes:

- `RULE_LOOKUP`
- `CALCULATION`
- `MIXED`
- `OUT_OF_SCOPE`
- `AMBIGUOUS`

## 3. Hindi and English

### Input

- Detect Devanagari and language at request time, while respecting explicit selection.
- Support code-mixed terms such as “setback kitna”, “FAR”, “building permission fee” and transliterated Hindi through reviewed aliases.
- Normalize Indian numeric forms (`5 lakh`) and units (`वर्ग मीटर`, `हेक्टेयर`) without losing the original phrase.

### Retrieval

The English JSONL is the primary machine-readable corpus. For Hindi queries, use a combination of multilingual embeddings and a reviewed bilingual terminology map. Query translation may expand retrieval but must never be stored/displayed as official rule text.

The official Hindi PDF uses a legacy font and is documented as extracting incorrectly. Therefore:

- do not bulk-ingest corrupted extracted text;
- visually transcribe only high-value excerpts with two-person review;
- link each verified Hindi excerpt to the same canonical provision and its PDF page;
- label generated Hindi explanations as explanations, not official quotations;
- fall back to canonical English exact text plus a Hindi explanation when verified Hindi text is unavailable.

### Output

- Answer in the chosen language.
- Keep rule identifiers, values and units unambiguous.
- Use an approved glossary for terms such as front open space, rear open space, built-up area, FAR, development permission and competent authority.
- Do not translate names of Acts/Rules in a way that obscures their official identity; provide both names where helpful.

## 4. Numerical reasoning boundary

The LLM may identify likely calculator intent and propose structured inputs. It shall not select a final table row or perform authoritative arithmetic.

Each Java calculator must:

- accept `BigDecimal`, explicit units/enums and `LocalDate asOfDate`;
- reject incomplete, negative, contradictory or unsupported inputs;
- select an active, reviewed `calculation_definition` and exact band;
- express lower/upper inclusivity in code/tests (`>9`, `<=12`, etc.);
- avoid binary floating-point for money/measurements;
- return a structured derivation and source citation;
- never extrapolate outside defined bands;
- be independently callable without the AI provider.

Examples from the supplied corpus that require boundary tests:

- Rule 56 front open space: `<=9 m -> 3.0 m`; `>9 and <=12 -> 3.6 m`; `>12 and <=18 -> 4.5 m`; `>18 -> 6.0 m`, for the stated residential height applicability.
- Rule 56 rear open space: `<=40 -> nil`; `>40 and <=150 -> 1.50 m`; `>150 and <=225 -> 2.50 m`; `>225 -> 3.00 m`.
- Rule 21 residential permission fee bands and commercial/shop-cum-residence percentage additions; distinguish high-rise per-square-meter rules.
- Rule 17 farmhouse: minimum 0.40 hectare, FAR 0.10, maximum height 7.5 m, road >=7.5 m and MOS 7.5 m.
- Rule 52: low/medium voltage 2.5 m vertical and 1.2 m horizontal, but applicable Electricity Rules govern if stricter.

## 5. Amendment model

### Source-of-truth policy

The official English PDF consolidated to 6 March 2025 remains an immutable base source. Later notifications are additive source documents, never edits to that PDF or its extracted JSONL.

Every amendment record must capture:

- notification identifier and issuing authority;
- official publication URL/file and checksum;
- publication and effective date (which may differ);
- affected canonical provision and operation (`INSERT`, `REPLACE`, `DELETE`, `RENUMBER`);
- old text, new text and human-readable effect summary;
- page/section locators;
- reviewer/approver and lifecycle state.

### 28 August 2026 amendment

Repository documentation says Rule 2(5)(c), concerning plots up to 500 sq m, changed on 28 August 2026 and is absent from the March 2025 source pack. Before activating it:

1. Obtain and retain the official notification/gazette, not only a secondary summary link.
2. Verify notification number, exact wording, commencement date and affected cross-references.
3. Create a new effective-dated provision linked with `SUPERSEDES` to the prior Rule 2(5)(c).
4. Add Hindi terminology/explanation, clearly separating official versus translated text.
5. Add before/on/after-effective-date tests, especially sample question 11 for a 300 sq m plot.
6. Activate a new corpus version only after maker-checker review.

Until those steps are complete, current-date answers affected by the amendment must return `NEEDS_REVIEW` with a clear cutoff warning, not the obsolete March 2025 answer.

### Effective-date resolution

For `asOfDate`, select provisions satisfying `valid_from <= asOfDate` and (`valid_to` is null or `asOfDate < valid_to`). Detect overlaps/gaps for the same canonical provision as ingestion errors. Answers must list all source versions used and warn if the question date is ambiguous.

## 6. Citation validation

For each generated claim:

1. Citation ID exists among retrieved/approved chunks.
2. Provision is effective on the answer date.
3. Returned excerpt matches normalized stored text and fits within cited page metadata.
4. Numbers and units in the claim equal the calculator output or cited source.
5. Qualifiers such as occupancy, height and “whichever is more” are retained.
6. No retrieved chunk is cited merely because it is topically similar.

If any legal claim lacks evidence, do not display a partial confident answer. Return a qualified `NEEDS_REVIEW`, or omit the unsupported claim if the remaining answer still directly answers the question.

## 7. Prompt/output contract

Require schema-constrained model output with:

- `answerLanguage`
- `statusProposal`
- `answerSegments[]` containing plain text and `citationIds[]`
- `assumptions[]`
- `missingFacts[]`
- `warnings[]`

The model does not emit source excerpts, page numbers, source URLs, calculator values or version dates independently. Spring Boot enriches these from trusted services.

## 8. RAG quality metrics

Track by language and question type:

- retrieval recall@5/10 for expected provisions;
- top-1/top-3 rule accuracy;
- citation precision and exact-excerpt validity;
- grounded-claim rate;
- numeric exact match and unit accuracy;
- not-found precision/recall;
- amendment/effective-date accuracy;
- Hindi semantic parity with the English expected answer;
- P50/P95 latency and provider failure rate.

The release gate is defined in the testing plan; model self-evaluation is not accepted as the sole grader.

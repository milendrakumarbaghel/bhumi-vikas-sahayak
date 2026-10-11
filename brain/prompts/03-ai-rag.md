# Prompt: AI/RAG, Citations and Hindi/English Grounding

Work in `/Users/milendrakumarbaghel/Documents/MPSeDC/bhumi-vikas-sahayak`.

Implement the AI/RAG workstream in the separate top-level `ai-service/`, using its internal contract with the Spring backend and `brain/05-ai-rag-and-amendments.md`. Read every `brain/` document and inspect both services before modifying code. Preserve contracts and unrelated changes.

Required behavior:

- Keep authoritative exact/full-text retrieval and effective-date filters in the backend/PostgreSQL. Put embedding generation, semantic reranking and model-specific adapters in `ai-service/` behind internal endpoints.
- Filter by corpus version, language metadata and legal effective date before context reaches the model.
- Implement query intent/fact extraction for lookup, calculation, mixed, ambiguous and out-of-scope requests. Explicit user facts win unless contradictory, in which case return `NEEDS_INPUT`.
- Support English, Hindi/Devanagari and common code-mixed/transliterated terms. Use the English JSONL as canonical machine-readable law. Do not ingest corrupted Hindi PDF extraction.
- Extend configurable embedding and generation interfaces inside `ai-service/` with deterministic fakes. Keep provider/model IDs in trace metadata. Provider keys exist only in the AI-service environment.
- Require schema-constrained model output with answer segments referring only to supplied chunk IDs.
- Implement citation validation: existing chunk ID, effective provision, exact normalized excerpt, numeric/unit consistency, and claim coverage.
- Enrich excerpts/pages/source URLs from trusted database records, not model output.
- Fail closed to `NOT_FOUND_IN_RULES` or `NEEDS_REVIEW`; one bounded regeneration attempt is acceptable, but never show an ungrounded fallback.
- Keep prompts resistant to user/source prompt injection. Do not log raw secrets, chain-of-thought or unrestricted personal queries.

Evaluation/tests:

- Build a versioned golden set from all 17 supplied sample questions with reviewed expected status and rules; do not invent expected legal conclusions where review is pending.
- Add paraphrases, Hindi/code-mixed cases, adjacent-rule distractors, exact rule-number searches, prompt-injection cases and out-of-domain cases.
- Report retrieval recall@5/10, top-rule accuracy, citation validity, not-found results, Hindi parity and latency separately.
- Property-tax and Kolar Road market-rate questions must return not found without invented citations.
- Provider timeout/malformed JSON/unknown citation must not produce an answered response.

Deliverables:

- AI-service provider/prompt/embedding code plus the narrow backend integration and citation-validation tests. Do not move legal authority into the AI service.
- Configuration documentation with safe placeholders only.
- An evaluation report with sample size, failures and known limitations; do not claim broad accuracy from the 17 seed cases.

Do not implement authoritative arithmetic in prompts or the model. Call calculator interfaces or return `NEEDS_INPUT`/`NEEDS_REVIEW`.

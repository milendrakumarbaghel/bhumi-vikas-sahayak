from __future__ import annotations

from html import escape
import json
import unicodedata
from typing import Any

from ai_service.calculators import calculators
from ai_service.models import AnswerProposal, CitationProposal, ClaimProposal, Measure
from ai_service.providers.base import ProviderError, StructuredLlmProvider
from ai_service.services.query_intent import QueryIntentService


STATUSES = ("answered", "not_found", "needs_more_info")
MAX_CHUNKS = 10
MAX_CHUNK_CHARS = 12_000

SYSTEM_PROMPT = """
You help a building-permission clerk at the Bhopal Municipal Corporation answer a citizen
at the counter. You answer ONLY from the rule extracts given in the user message. They come
from the M.P. Bhumi Vikas Rules, 2012, consolidated to 6 March 2025. The clerk decides; you
point to the rule. This is not legal advice.

Rules you must follow:
1. Use only the provided extracts. Never use outside knowledge. Never invent a rule, a rule
   number, a fee, a distance or a condition.
2. If the extracts do not answer the question, set status "not_found" and say plainly that
   the loaded rules do not cover it. Give no citations.
3. If the answer depends on a fact the question does not give (for example road width,
   plot area, built-up area, building height or building use), set status "needs_more_info",
   list each missing fact in missingInputs using short English labels such as "road width",
   "plot area", "built-up area", "building height", "building use", ask for them in the
   answer, and cite the rule that needs them.
4. Every statement in an answer must be backed by a citation: the chunkId of the extract and
   a quote copied character for character from that extract's text. Quote one sentence or
   one table row, at most about 300 characters. Do not fix spelling, do not join text from
   different places, do not add "..." and do not translate the quote.
5. Keep the answer to 2-4 short sentences. Give numbers and units exactly as the rule does
   and name the rule number, e.g. "Rule 56(1)".
6. If a CALCULATION is provided, it was computed by code from the rule tables: use its
   result values, units and rule as given, and briefly mention its assumptions (for example
   "assuming the building is up to 12.5 m high"). Still cite the rule text it came from.
   Anything under "floorIllustration" is NOT from the rules: if you mention it, say clearly
   that it is only an illustration because the rules do not set ground coverage. Never do
   arithmetic yourself. If a figure would need arithmetic and no calculation is provided,
   quote the rule's rate or table row instead of computing.
7. Every number in your answer (fees, metres, years, percentages) must appear in the
   extracts, the question or the CALCULATION. The backend checks every quote and number
   against the rule text and rejects the whole answer if any does not match.
8. Language: write the answer in the language given as ANSWER_LANGUAGE. For "hi" use simple,
   plain Hindi in Devanagari that a citizen understands; keep rule numbers (e.g. "नियम 56"),
   numbers and units as digits, and keep quotes in the original English.
9. Return claims: one entry for each sentence, in the answer's exact order. Concatenating
   claim texts with spaces must reproduce the answer exactly. Each claim must list the
   chunkIds whose cited quotes support that complete sentence, including every condition,
   exception and legal modality (may/must/prohibited). Do not hide unsupported sentences.
   Use an empty claims list only for not_found. Another independent verifier checks entailment.
""".strip()


class AnswerService:
    def __init__(
        self,
        provider: StructuredLlmProvider,
        intent_service: QueryIntentService | None = None,
    ) -> None:
        self._provider = provider
        self._intent = intent_service

    def answer(self, request: dict[str, Any]) -> AnswerProposal:
        question = _required_string(request, "question", 600)
        language = "hi" if request.get("language") == "hi" else "en"
        chunks = _chunks(request.get("chunks"))
        if not self._provider.configured:
            raise ProviderError("LLM provider is not configured")

        calculation = request.get("calculation")
        if not (isinstance(calculation, dict) and calculation):
            calculation = self._calculate(request, question)

        raw = self._provider.complete_json(
            SYSTEM_PROMPT,
            _user_message(question, language, chunks, calculation),
            response_schema(),
            effort="medium",
        )
        try:
            proposed = json.loads(raw)
        except json.JSONDecodeError as exc:
            raise ProviderError("Provider returned invalid structured JSON") from exc
        if not isinstance(proposed, dict) or proposed.get("status") not in STATUSES:
            raise ProviderError("Provider response does not match the answer schema")

        known_ids = {chunk["id"] for chunk in chunks}
        citations = tuple(
            CitationProposal(item["chunkId"], item["quote"])
            for item in proposed.get("citations", [])
            if isinstance(item, dict)
            and item.get("chunkId") in known_ids
            and isinstance(item.get("quote"), str)
            and item["quote"].strip()
        )
        return AnswerProposal(
            status=proposed["status"],
            language=language,
            answer=str(proposed.get("answer", "")).strip(),
            citations=citations,
            missing_inputs=tuple(
                item for item in proposed.get("missingInputs", []) if isinstance(item, str)
            ),
            model=self._provider.model_name,
            calculations=calculation,
            claims=tuple(ClaimProposal(str(item.get("text","")),tuple(item.get("chunkIds",[])))
                         for item in proposed.get("claims",[]) if isinstance(item,dict)
                         and isinstance(item.get("text"),str)
                         and isinstance(item.get("chunkIds"),list)
                         and all(isinstance(value,str) for value in item["chunkIds"])),
        )

    def _calculate(self, request: dict[str, Any], question: str) -> dict[str, Any] | None:
        """Runs the rule calculators on stated facts, so the model never does arithmetic.

        Facts come from the request ("facts"), or else from the query-intent analysis of the
        question. Returns None when no calculator applies.
        """
        facts_body = request.get("facts")
        if isinstance(facts_body, dict):
            topics = facts_body.get("topics") or request.get("topics") or []
        elif self._intent is not None:
            try:
                analysis = self._intent.analyse(question)
            except ProviderError:
                return None  # answer without a calculation rather than fail
            facts_body = {
                "plotArea": _measure(analysis.plot_area),
                "roadWidth": _measure(analysis.road_width),
                "plotFrontage": _measure(analysis.plot_frontage),
                "buildingHeight": _measure(analysis.building_height),
                "builtUpArea": _measure(analysis.built_up_area),
                "buildingUse": analysis.building_use,
                "electricLineCategory": analysis.electric_line_category,
                "buildingForm": analysis.building_form,
                "storeys": analysis.storeys,
            }
            topics = list(analysis.topics)
        else:
            return None
        try:
            facts = calculators.facts_from_contract(facts_body)
        except ValueError:
            if "facts" in request:
                raise  # the caller sent bad facts: report it as a validation error
            return None
        result = calculators.run(facts, list(topics))
        return result if result["calculations"] or result["notApplicable"] else None


def response_schema() -> dict[str, Any]:
    properties: dict[str, Any] = {
        "status": {"type": "string", "enum": list(STATUSES)},
        "answer": {"type": "string"},
        "citations": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "chunkId": {"type": "string"},
                    "quote": {"type": "string"},
                },
                "required": ["chunkId", "quote"],
                "additionalProperties": False,
            },
        },
        "missingInputs": {"type": "array", "items": {"type": "string"}},
        "claims": {"type":"array","items":{"type":"object","properties":{
            "text":{"type":"string"},"chunkIds":{"type":"array","items":{"type":"string"}}},
            "required":["text","chunkIds"],"additionalProperties":False}},
    }
    return {
        "type": "object",
        "properties": properties,
        "required": list(properties),
        "additionalProperties": False,
    }


def _user_message(
    question: str,
    language: str,
    chunks: list[dict[str, str]],
    calculation: Any,
) -> str:
    parts = [f"ANSWER_LANGUAGE: {language}", f"QUESTION:\n{question}", "RULE EXTRACTS:"]
    for chunk in chunks:
        parts.append(
            f'<extract chunkId="{escape(chunk["id"])}" label="{escape(chunk["label"])}" '
            f'title="{escape(chunk["title"])}" pdfPages="{escape(chunk["pages"])}">\n'
            f'{chunk["text"]}\n</extract>'
        )
    if isinstance(calculation, dict) and calculation:
        parts.append("CALCULATION (computed by the backend; use as given):\n"
                     + json.dumps(calculation, ensure_ascii=False, indent=2))
    return "\n\n".join(parts)


def _measure(value: Measure | None) -> dict[str, Any] | None:
    return value.to_dict() if value else None


def _chunks(value: Any) -> list[dict[str, str]]:
    if not isinstance(value, list) or len(value) > MAX_CHUNKS:
        raise ValueError(f"chunks must be a list of at most {MAX_CHUNKS} items")
    chunks = []
    for item in value:
        if not isinstance(item, dict):
            raise ValueError("each chunk must be an object")
        chunk = {key: _required_string(item, key, MAX_CHUNK_CHARS)
                 for key in ("id", "label", "title", "pages", "text")}
        chunks.append(chunk)
    return chunks


def _required_string(source: dict[str, Any], key: str, max_length: int) -> str:
    value = source.get(key)
    if not isinstance(value, str) or not value.strip():
        raise ValueError(f"{key} must be a non-blank string")
    value = unicodedata.normalize("NFC", value).strip()
    if len(value) > max_length:
        raise ValueError(f"{key} must be at most {max_length} characters")
    return value

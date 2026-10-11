from __future__ import annotations

import json
import re
import unicodedata
from typing import Any

from ai_service.models import Measure, QueryAnalysis
from ai_service.providers.base import ProviderError, StructuredLlmProvider


TOPICS = (
    "SETBACK", "FAR", "HEIGHT", "FEES", "PARKING", "FARM_HOUSE", "VALIDITY",
    "DEEMED_SANCTION", "ELECTRIC_LINES", "PLAN_SIGNING", "APPLICATION_DOCUMENTS",
    "PLAN_CHANGE", "LIFTS", "DEVELOPMENT_PLAN", "DEVELOPMENT_PERMISSION", "PLOT_SIZE",
    "BALCONY_PROJECTIONS", "BASEMENT", "FIRE",
)
BUILDING_USES = (
    "residential", "commercial", "shop_cum_residence", "industrial", "public",
    "agricultural", "other", "unknown",
)
ELECTRIC_LINE_CATEGORIES = (
    "low_medium", "high_up_to_33000", "extra_high_above_33000", "unknown",
)
BUILDING_FORMS = ("detached", "semi_detached", "row", "unknown")
NUMBER = re.compile(r"\d+(?:\.\d+)?")

SYSTEM_PROMPT = f"""
You prepare search input for a tool that answers questions only from the Madhya Pradesh
Bhumi Vikas Rules, 2012. Do not answer the legal question and do not add facts.

Return the user's language (en/hi), a faithful English translation, 5-15 English legal
search terms, applicable topic codes, explicitly stated measurements, building use, building
form (detached, semi-detached or row), electric line category, and
explicitly named rule references. Copy measurement numbers and units exactly; use null
when absent. Hindi, Devanagari and romanised Hindi may occur. "gaj" means sqyd.

Topic codes: {", ".join(TOPICS)}
""".strip()


class QueryIntentService:
    def __init__(self, provider: StructuredLlmProvider) -> None:
        self._provider = provider

    @property
    def provider_configured(self) -> bool:
        return self._provider.configured

    def analyse(self, question: str) -> QueryAnalysis:
        normalized = unicodedata.normalize("NFC", question).strip()
        if not normalized:
            raise ValueError("question must not be blank")
        if len(normalized) > 600:
            raise ValueError("question must be at most 600 characters")
        if not self._provider.configured:
            raise ProviderError("LLM provider is not configured")

        raw = self._provider.complete_json(
            SYSTEM_PROMPT,
            f"Question:\n{normalized}",
            response_schema(),
        )
        try:
            proposed = json.loads(raw)
        except json.JSONDecodeError as exc:
            raise ProviderError("Provider returned invalid structured JSON") from exc
        if not isinstance(proposed, dict):
            raise ProviderError("Provider response must be an object")

        stated_numbers = _numbers_in(normalized)
        return QueryAnalysis(
            language="hi" if proposed.get("language") == "hi" else "en",
            english_question=_string(proposed.get("english_question"), normalized),
            search_terms=_string(proposed.get("search_terms"), ""),
            topics=tuple(item for item in _strings(proposed.get("topics")) if item in TOPICS),
            plot_area=_verified_measure(proposed.get("plot_area"), stated_numbers),
            road_width=_verified_measure(proposed.get("road_width"), stated_numbers),
            plot_frontage=_verified_measure(proposed.get("plot_frontage"), stated_numbers),
            building_height=_verified_measure(proposed.get("building_height"), stated_numbers),
            built_up_area=_verified_measure(proposed.get("built_up_area"), stated_numbers),
            building_use=(
                proposed.get("building_use")
                if proposed.get("building_use") in BUILDING_USES
                else "unknown"
            ),
            electric_line_category=(
                proposed.get("electric_line_category")
                if proposed.get("electric_line_category") in ELECTRIC_LINE_CATEGORIES
                else "unknown"
            ),
            building_form=(
                proposed.get("building_form")
                if proposed.get("building_form") in BUILDING_FORMS else "unknown"
            ),
            rule_refs=tuple(_strings(proposed.get("rule_refs"))),
            model=self._provider.model_name,
            storeys=(proposed.get("storeys") if isinstance(proposed.get("storeys"),int)
                     and not isinstance(proposed.get("storeys"),bool) and proposed.get("storeys") > 0
                     and proposed.get("storeys") in stated_numbers else None),
        )


def response_schema() -> dict[str, Any]:
    properties: dict[str, Any] = {
        "language": {"type": "string", "enum": ["en", "hi"]},
        "english_question": {"type": "string"},
        "search_terms": {"type": "string"},
        "topics": {"type": "array", "items": {"type": "string", "enum": list(TOPICS)}},
        "plot_area": _nullable_measure(["sqm", "sqft", "sqyd"]),
        "road_width": _nullable_measure(["m", "ft"]),
        "plot_frontage": _nullable_measure(["m", "ft"]),
        "building_height": _nullable_measure(["m", "ft"]),
        "built_up_area": _nullable_measure(["sqm", "sqft", "sqyd"]),
        "building_use": {"type": "string", "enum": list(BUILDING_USES)},
        "electric_line_category": {"type": "string", "enum": list(ELECTRIC_LINE_CATEGORIES)},
        "building_form": {"type": "string", "enum": list(BUILDING_FORMS)},
        "storeys": {"anyOf": [{"type":"integer","minimum":1},{"type":"null"}]},
        "rule_refs": {"type": "array", "items": {"type": "string"}},
    }
    return {
        "type": "object",
        "properties": properties,
        "required": list(properties),
        "additionalProperties": False,
    }


def _nullable_measure(units: list[str]) -> dict[str, Any]:
    return {
        "anyOf": [
            {
                "type": "object",
                "properties": {
                    "value": {"type": "number"},
                    "unit": {"type": "string", "enum": units},
                },
                "required": ["value", "unit"],
                "additionalProperties": False,
            },
            {"type": "null"},
        ]
    }


def _verified_measure(value: Any, stated_numbers: set[float]) -> Measure | None:
    if not isinstance(value, dict):
        return None
    number = value.get("value")
    unit = value.get("unit")
    if isinstance(number, bool) or not isinstance(number, (int, float)):
        return None
    if float(number) not in stated_numbers or not isinstance(unit, str):
        return None
    return Measure(float(number), unit)


def _numbers_in(text: str) -> set[float]:
    ascii_text = "".join(
        str(unicodedata.digit(character)) if character.isdigit() else character
        for character in text.replace(",", "")
    )
    return {float(match.group()) for match in NUMBER.finditer(ascii_text)}


def _strings(value: Any) -> list[str]:
    if not isinstance(value, list):
        return []
    return [item for item in value if isinstance(item, str)]


def _string(value: Any, fallback: str) -> str:
    return value if isinstance(value, str) and value.strip() else fallback

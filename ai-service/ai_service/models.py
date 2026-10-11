from __future__ import annotations

from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True)
class Measure:
    value: float
    unit: str

    def to_dict(self) -> dict[str, Any]:
        return {"value": self.value, "unit": self.unit}


@dataclass(frozen=True)
class QueryAnalysis:
    language: str
    english_question: str
    search_terms: str
    topics: tuple[str, ...]
    plot_area: Measure | None
    road_width: Measure | None
    plot_frontage: Measure | None
    building_height: Measure | None
    built_up_area: Measure | None
    building_use: str
    electric_line_category: str
    building_form: str
    rule_refs: tuple[str, ...]
    model: str
    storeys: int | None = None

    def to_contract(self) -> dict[str, Any]:
        """Return the camelCase contract already consumed by the Java backend."""
        return {
            "language": self.language,
            "englishQuestion": self.english_question,
            "searchTerms": self.search_terms,
            "topics": list(self.topics),
            "plotArea": _measure(self.plot_area),
            "roadWidth": _measure(self.road_width),
            "plotFrontage": _measure(self.plot_frontage),
            "buildingHeight": _measure(self.building_height),
            "builtUpArea": _measure(self.built_up_area),
            "buildingUse": self.building_use,
            "electricLineCategory": self.electric_line_category,
            "buildingForm": self.building_form,
            "ruleRefs": list(self.rule_refs),
            "model": self.model,
            "storeys": self.storeys,
        }


def _measure(value: Measure | None) -> dict[str, Any] | None:
    return value.to_dict() if value else None


@dataclass(frozen=True)
class CitationProposal:
    chunk_id: str
    quote: str


@dataclass(frozen=True)
class ClaimProposal:
    text: str
    chunk_ids: tuple[str, ...]


@dataclass(frozen=True)
class AnswerProposal:
    """An unverified answer. The Java backend checks every quote before showing it."""

    status: str
    language: str
    answer: str
    citations: tuple[CitationProposal, ...]
    missing_inputs: tuple[str, ...]
    model: str
    # Output of ai_service.calculators.run(), or None when no calculator applied.
    calculations: dict[str, Any] | None = None
    claims: tuple[ClaimProposal, ...] = ()

    def to_contract(self) -> dict[str, Any]:
        return {
            "status": self.status,
            "language": self.language,
            "answer": self.answer,
            "citations": [{"chunkId": c.chunk_id, "quote": c.quote} for c in self.citations],
            "missingInputs": list(self.missing_inputs),
            "model": self.model,
            "calculations": self.calculations,
            "claims": [{"text": c.text,"chunkIds": list(c.chunk_ids)} for c in self.claims],
        }

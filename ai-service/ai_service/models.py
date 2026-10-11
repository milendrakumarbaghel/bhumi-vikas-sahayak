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
    building_height: Measure | None
    built_up_area: Measure | None
    building_use: str
    rule_refs: tuple[str, ...]
    model: str

    def to_contract(self) -> dict[str, Any]:
        """Return the camelCase contract already consumed by the Java backend."""
        return {
            "language": self.language,
            "englishQuestion": self.english_question,
            "searchTerms": self.search_terms,
            "topics": list(self.topics),
            "plotArea": _measure(self.plot_area),
            "roadWidth": _measure(self.road_width),
            "buildingHeight": _measure(self.building_height),
            "builtUpArea": _measure(self.built_up_area),
            "buildingUse": self.building_use,
            "ruleRefs": list(self.rule_refs),
            "model": self.model,
        }


def _measure(value: Measure | None) -> dict[str, Any] | None:
    return value.to_dict() if value else None

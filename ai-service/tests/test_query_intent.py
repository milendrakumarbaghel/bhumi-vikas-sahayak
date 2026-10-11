from __future__ import annotations

import json
import unittest
from typing import Any

from ai_service.services.query_intent import QueryIntentService


class FixedProvider:
    configured = True
    model_name = "test-model"

    def complete_json(
        self,
        system_prompt: str,
        user_message: str,
        json_schema: dict[str, Any],
    ) -> str:
        return json.dumps({
            "language": "en",
            "english_question": "My plot is 200 sqm on a 9 m road",
            "search_terms": "plot road open space",
            "topics": ["SETBACK", "UNKNOWN_TOPIC"],
            "plot_area": {"value": 200, "unit": "sqm"},
            "road_width": {"value": 9, "unit": "m"},
            "building_height": {"value": 99, "unit": "m"},
            "built_up_area": None,
            "building_use": "unknown",
            "rule_refs": [],
        })


class QueryIntentServiceTest(unittest.TestCase):
    def test_rejects_hallucinated_measurements_and_unknown_topics(self) -> None:
        result = QueryIntentService(FixedProvider()).analyse(
            "My plot is 200 sqm on a 9 m road"
        )

        self.assertEqual(200.0, result.plot_area.value)
        self.assertEqual(9.0, result.road_width.value)
        self.assertIsNone(result.building_height)
        self.assertEqual(("SETBACK",), result.topics)
        self.assertEqual("test-model", result.model)

    def test_supports_devanagari_digits(self) -> None:
        result = QueryIntentService(FixedProvider()).analyse(
            "मेरा प्लॉट २०० sqm है और सड़क ९ m है"
        )

        self.assertEqual(200.0, result.plot_area.value)
        self.assertEqual(9.0, result.road_width.value)


if __name__ == "__main__":
    unittest.main()

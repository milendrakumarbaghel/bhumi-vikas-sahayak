from __future__ import annotations

from decimal import Decimal as D
from http.server import ThreadingHTTPServer
import json
from threading import Thread
import unittest
from urllib import request

from ai_service.app import create_handler
from ai_service.calculators import calculators as c
from ai_service.services.answer import AnswerService
from ai_service.services.query_intent import QueryIntentService
from tests.test_answer import CHUNK, ReplyProvider, answered
from tests.test_query_intent import FixedProvider


def value(result: c.Calculation) -> float:
    assert isinstance(result, c.Calculation), result
    return result.result["value"]


class OpenSpaceTest(unittest.TestCase):
    """Rule 56(1)(a), PDF p.77 and Rule 56(2)(a), PDF p.78 - band edges are inclusive upper limits."""

    def test_front_open_space_bands(self) -> None:
        cases = {"6": 3.0, "9": 3.0, "9.01": 3.6, "10": 3.6, "12": 3.6, "12.5": 4.5, "18": 4.5, "18.5": 6.0, "30": 6.0}
        for road, expected in cases.items():
            with self.subTest(road=road):
                self.assertEqual(expected, value(c.front_open_space(c.Facts(road_width_m=D(road)))))

    def test_rear_open_space_bands(self) -> None:
        cases = {"40": 0.0, "40.5": 1.5, "150": 1.5, "150.5": 2.5, "225": 2.5, "226": 3.0}
        for area, expected in cases.items():
            with self.subTest(area=area):
                self.assertEqual(expected, value(c.rear_open_space(c.Facts(plot_area_sqm=D(area)))))
        self.assertEqual("Nil", c.rear_open_space(c.Facts(plot_area_sqm=D(30))).result["text"])

    def test_side_open_space_uses_form_height_and_frontage(self) -> None:
        result = c.side_open_space(c.Facts(
            building_use="residential", building_form="detached",
            building_height_m=D("7"), plot_frontage_m=D("11"),
        ))
        self.assertIsInstance(result, c.Calculation)
        self.assertEqual(1.5, result.result["value"])
        self.assertEqual("one side", result.result["requiredOn"])

    def test_states_assumptions_and_refuses_tall_or_non_residential(self) -> None:
        calc = c.front_open_space(c.Facts(road_width_m=D(10)))
        self.assertIn("Assumes the building is up to 12.5 m high.", calc.assumptions)
        tall = c.front_open_space(c.Facts(road_width_m=D(10), building_height_m=D(15)))
        self.assertIsInstance(tall, c.NotApplicable)
        self.assertIn("Rule 42", tall.reason)
        shop = c.front_open_space(c.Facts(road_width_m=D(10), building_use="commercial"))
        self.assertIsInstance(shop, c.NotApplicable)

    def test_reports_missing_inputs(self) -> None:
        self.assertEqual(c.Missing("road width"), c.front_open_space(c.Facts()))
        self.assertEqual(c.Missing("plot area"), c.rear_open_space(c.Facts()))


class FeeTest(unittest.TestCase):
    """Rule 21(3)(b), PDF p.36; Rule 21(3)(c), PDF p.37; Rule 2(38), PDF p.8; Rule 21(2)(ii), PDF p.35."""

    def fee(self, built_up: str, **kwargs) -> float:
        return value(c.building_permission_fee(c.Facts(built_up_area_sqm=D(built_up), **kwargs)))

    def test_residential_bands_including_the_gaps(self) -> None:
        cases = {"50": 750, "75": 750, "75.5": 1250, "76": 1250, "125": 1250, "200": 2000, "250": 3000,
                 "2500": 25000, "2500.5": 50000, "9000": 50000}
        for area, expected in cases.items():
            with self.subTest(area=area):
                self.assertEqual(expected, self.fee(area, building_use="residential", storeys=2))

    def test_surcharges(self) -> None:
        self.assertEqual(3000, self.fee("200", building_use="commercial", storeys=2))          # 2,000 + 50%
        self.assertEqual(3000, self.fee("200", building_use="shop_cum_residence", storeys=2))  # 2,000 + 50%
        self.assertEqual(2000, self.fee("200", building_use="public", storeys=2))              # item 6 = item 1

    def test_high_rise(self) -> None:
        self.assertEqual(50000, self.fee("5000", building_use="residential", storeys=11))           # Rs. 10 x 5000
        self.assertEqual(100000, self.fee("5000", building_use="commercial", building_height_m=D(31)))  # +100%
        self.assertEqual(50000, self.fee("5000", building_use="residential", storeys=10, building_height_m=D(30)),
                         "10 storeys / 30 m is not high rise, so the band table applies")

    def test_uses_far_area_when_only_plot_is_given(self) -> None:
        calc = c.building_permission_fee(c.Facts(plot_area_sqm=D(200)))
        self.assertEqual(3000, value(calc))  # 200 x 1.25 = 250 sq m -> 201-300 band
        self.assertTrue(any("maximum built-up area under FAR" in a for a in calc.assumptions))
        self.assertEqual("Rs. 3,000", calc.result["text"])

    def test_application_fee(self) -> None:
        self.assertEqual(250.0, value(c.application_fee(c.Facts(built_up_area_sqm=D(250)))))
        self.assertEqual(c.Missing("built-up area"), c.application_fee(c.Facts()))

    def test_rupee_formatting(self) -> None:
        self.assertEqual("Rs. 1,25,000", c.rupees(D(125000)))
        self.assertEqual("Rs. 750", c.rupees(D(750)))


class FarTest(unittest.TestCase):
    """Rule 61 Table 8, PDF p.83; Rule 17, PDF p.34."""

    def test_residential_max_built_up_and_labelled_floor_illustration(self) -> None:
        calc = c.floor_area(c.Facts(plot_area_sqm=D(200)))
        self.assertEqual(250.0, value(calc))
        self.assertIn("NOT FROM THE RULES", calc.result["floorIllustrationLabel"])
        self.assertEqual([2.5, 2.1, 1.7], [x["floors"] for x in calc.result["floorIllustration"]])
        custom = c.floor_area(c.Facts(plot_area_sqm=D(200), ground_coverage_percent=D(50)))
        self.assertEqual([2.5], [x["floors"] for x in custom.result["floorIllustration"]])

    def test_other_uses(self) -> None:
        self.assertEqual(200.0, value(c.floor_area(c.Facts(plot_area_sqm=D(200), building_use="public"))))
        self.assertIsInstance(c.floor_area(c.Facts(plot_area_sqm=D(200), building_use="commercial")), c.Missing)
        self.assertEqual(500.0, value(c.floor_area(c.Facts(plot_area_sqm=D(200), building_use="commercial",
                                                           commercial_category="city_centre"))))
        self.assertIsInstance(c.floor_area(c.Facts(plot_area_sqm=D(200), building_use="industrial")), c.NotApplicable)

    def test_farm_house(self) -> None:
        ok = c.farm_house(c.Facts(plot_area_sqm=D(4000)))
        self.assertTrue(ok.result["meetsMinimumPlot"])
        self.assertEqual(400.0, value(ok))
        small = c.farm_house(c.Facts(plot_area_sqm=D(3999)))
        self.assertFalse(small.result["meetsMinimumPlot"])
        self.assertTrue(small.notes)


class RunAndContractTest(unittest.TestCase):
    def test_counter_question_200_sqm_on_9_m_road(self) -> None:
        facts = c.facts_from_contract({"plotArea": {"value": 200, "unit": "sqm"},
                                       "roadWidth": {"value": 9, "unit": "m"}})
        result = c.run(facts, ["SETBACK", "FAR", "FEES"])
        by_code = {x["code"]: x["result"]["value"] for x in result["calculations"]}
        self.assertEqual({"RESIDENTIAL_FRONT_OPEN_SPACE": 3.0, "RESIDENTIAL_REAR_OPEN_SPACE": 2.5,
                          "MAX_BUILT_UP_AREA_FAR": 250.0, "BUILDING_PERMISSION_FEE": 3000.0,
                          "BUILDING_PERMISSION_APPLICATION_FEE": 250.0}, by_code)
        for calc in result["calculations"]:
            self.assertIn(calc["pdfPage"], (36, 35, 77, 78, 83))

    def test_units_are_converted_in_code(self) -> None:
        facts = c.facts_from_contract({"plotArea": {"value": 1200, "unit": "sqft"},
                                       "roadWidth": {"value": 30, "unit": "ft"}})
        self.assertAlmostEqual(111.48, float(facts.plot_area_sqm), places=2)
        self.assertAlmostEqual(9.144, float(facts.road_width_m), places=3)
        self.assertEqual(4000, c.facts_from_contract({"plotArea": {"value": 0.4, "unit": "ha"}}).plot_area_sqm)

    def test_rejects_bad_input(self) -> None:
        for body in ({"plotArea": {"value": -5, "unit": "sqm"}}, {"plotArea": {"value": 5, "unit": "acre"}},
                     {"buildingUse": "palace"}, {"storeys": 0}, {"groundCoveragePercent": 150}):
            with self.subTest(body=body), self.assertRaises(ValueError):
                c.facts_from_contract(body)

    def test_height_electric_clearance_and_validity(self) -> None:
        height = c.run(c.Facts(building_height_m=D("31")), ["HEIGHT"])
        self.assertTrue(height["calculations"][0]["result"]["value"])
        clearance = c.run(c.Facts(electric_line_category="low_medium"), ["ELECTRIC_LINES"])
        self.assertEqual(2.5, clearance["calculations"][0]["result"]["verticalM"])
        self.assertEqual(1.2, clearance["calculations"][0]["result"]["horizontalM"])
        validity = c.run(c.Facts(), ["VALIDITY"])
        self.assertEqual(5, validity["calculations"][0]["result"]["maximumYears"])


class EndpointAndAnswerTest(unittest.TestCase):
    def test_calculations_endpoint_needs_no_provider(self) -> None:
        handler = create_handler(QueryIntentService(FixedProvider()), "secret")
        server = ThreadingHTTPServer(("127.0.0.1", 0), handler)
        Thread(target=server.serve_forever, daemon=True).start()
        try:
            body = json.dumps({"topics": ["SETBACK"], "roadWidth": {"value": 10, "unit": "m"}}).encode()
            req = request.Request(f"http://127.0.0.1:{server.server_port}/internal/v1/calculations", data=body,
                                  method="POST", headers={"content-type": "application/json", "x-internal-token": "secret"})
            with request.urlopen(req) as response:
                payload = json.load(response)
            self.assertEqual(3.6, payload["calculations"][0]["result"]["value"])
            self.assertEqual(
                ["plot area", "building form (detached, semi-detached or row)"],
                payload["missingInputs"],
            )
        finally:
            server.shutdown()
            server.server_close()

    def test_answers_pass_calculation_from_request_facts_to_the_model(self) -> None:
        provider = ReplyProvider(answered([]))
        result = AnswerService(provider).answer({
            "question": "200 sqm plot on 9 m road, front gap?", "chunks": [CHUNK],
            "facts": {"topics": ["SETBACK"], "plotArea": {"value": 200, "unit": "sqm"},
                      "roadWidth": {"value": 9, "unit": "m"}},
        })
        self.assertIn("CALCULATION", provider.last_user_message)
        self.assertIn("RESIDENTIAL_FRONT_OPEN_SPACE", provider.last_user_message)
        self.assertEqual("RESIDENTIAL_FRONT_OPEN_SPACE", result.calculations["calculations"][0]["code"])
        self.assertIn("calculations", result.to_contract())

    def test_answers_derive_facts_from_intent_analysis_when_none_are_sent(self) -> None:
        # FixedProvider's intent: SETBACK, 200 sqm plot, 9 m road (its invented 99 m height is dropped).
        provider = ReplyProvider(answered([]))
        result = AnswerService(provider, QueryIntentService(FixedProvider())).answer({
            "question": "My plot is 200 sqm on a 9 m road", "chunks": [CHUNK]})
        codes = [x["code"] for x in result.calculations["calculations"]]
        self.assertEqual(["RESIDENTIAL_FRONT_OPEN_SPACE", "RESIDENTIAL_REAR_OPEN_SPACE"], codes)
        self.assertEqual(3.0, result.calculations["calculations"][0]["result"]["value"])

    def test_no_calculation_when_nothing_applies(self) -> None:
        provider = ReplyProvider(answered([]))
        result = AnswerService(provider).answer({"question": "Who signs plans?", "chunks": [CHUNK]})
        self.assertIsNone(result.calculations)
        self.assertNotIn("CALCULATION", provider.last_user_message)


if __name__ == "__main__":
    unittest.main()

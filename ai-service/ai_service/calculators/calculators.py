"""Deterministic calculators for the M.P. Bhumi Vikas Rules, 2012.

Plain Python, no language model: the model only words the result. Every result names the
rule and PDF page it comes from, shows its inputs and formula, and lists any assumption.
Values come from tables.py, which cites the PDF page for each one.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from decimal import ROUND_HALF_UP, Decimal
from typing import Any

from ai_service.calculators import tables as t

SQFT_PER_SQM = Decimal("10.763910416709722")
SQM_PER_SQYD = Decimal("0.83612736")
M_PER_FT = Decimal("0.3048")

# Ground-coverage levels used only to illustrate a floor count. NOT from the rules.
ILLUSTRATIVE_COVERAGE_PERCENT = (Decimal("50"), Decimal("60"), Decimal("75"))


@dataclass(frozen=True)
class Facts:
    """Inputs in metres / square metres. None means the question did not give it."""

    plot_area_sqm: Decimal | None = None
    road_width_m: Decimal | None = None
    plot_frontage_m: Decimal | None = None
    building_height_m: Decimal | None = None
    built_up_area_sqm: Decimal | None = None
    storeys: int | None = None
    building_use: str = "unknown"
    commercial_category: str | None = None
    ground_coverage_percent: Decimal | None = None
    electric_line_category: str = "unknown"
    building_form: str = "unknown"


@dataclass(frozen=True)
class Missing:
    """The calculator needs an input the question did not give."""

    input: str


@dataclass(frozen=True)
class NotApplicable:
    """The calculator does not cover this case; the reason is shown to the clerk."""

    reason: str


@dataclass
class Calculation:
    code: str
    rule: str
    pdf_page: int
    inputs: list[dict[str, Any]]
    formula: str
    result: dict[str, Any]
    matched_band: str = ""
    assumptions: list[str] = field(default_factory=list)
    notes: list[str] = field(default_factory=list)

    def to_contract(self) -> dict[str, Any]:
        return {
            "code": self.code,
            "rule": self.rule,
            "pdfPage": self.pdf_page,
            "inputs": self.inputs,
            "matchedBand": self.matched_band,
            "formula": self.formula,
            "result": self.result,
            "assumptions": self.assumptions,
            "notes": self.notes,
        }


# ------------------------------------------------------------------ unit conversion

def area_to_sqm(value: Decimal, unit: str) -> Decimal:
    unit = (unit or "sqm").lower()
    if unit == "sqft":
        return value / SQFT_PER_SQM
    if unit == "sqyd":
        return value * SQM_PER_SQYD
    if unit in ("ha", "hectare"):
        return value * t.SQM_PER_HECTARE
    return value


def length_to_m(value: Decimal, unit: str) -> Decimal:
    return value * M_PER_FT if (unit or "m").lower() == "ft" else value


def _num(value: Decimal, places: str = "0.01") -> float:
    return float(value.quantize(Decimal(places), rounding=ROUND_HALF_UP))


def rupees(value: Decimal) -> str:
    """Indian digit grouping: 125000 -> "Rs. 1,25,000"."""
    whole = str(int(value.quantize(Decimal("1"), rounding=ROUND_HALF_UP)))
    if len(whole) > 3:
        head, tail = whole[:-3], whole[-3:]
        groups = []
        while len(head) > 2:
            groups.insert(0, head[-2:])
            head = head[:-2]
        whole = ",".join([head] + groups + [tail]) if head else ",".join(groups + [tail])
    return "Rs. " + whole


def _measure(name: str, value: Decimal, unit: str) -> dict[str, Any]:
    return {"name": name, "value": _num(value), "unit": unit}


def _band(value: Decimal, bands: tuple, unit: str) -> tuple[Decimal, str]:
    """Picks the first band whose upper limit (inclusive) is >= value."""
    lower = None
    for upper, result in bands:
        if upper is None or value <= upper:
            if lower is None:
                text = f"up to {upper} {unit}"
            elif upper is None:
                text = f"above {lower} {unit}"
            else:
                text = f"above {lower} {unit} and up to {upper} {unit}"
            return result, text
        lower = upper
    raise ValueError("bands must end with an open upper limit")


def _low_rise_assumptions(facts: Facts) -> tuple[list[str], NotApplicable | None]:
    """Rule 56(1)-(2) apply only to residential buildings up to 12.5 m high."""
    assumptions = []
    if facts.building_use not in ("residential", "unknown"):
        return [], NotApplicable("Rule 56 open spaces in this table are for residential buildings only.")
    if facts.building_use == "unknown":
        assumptions.append("Assumes a residential building.")
    if facts.building_height_m is None:
        assumptions.append("Assumes the building is up to 12.5 m high.")
    elif facts.building_height_m > t.LOW_RISE_HEIGHT_LIMIT_M:
        return [], NotApplicable("The building is above 12.5 m, so Rule 56(5) applies: open spaces "
                                 "come from Tables 4 and 5 of Rule 42, which this calculator does not cover.")
    return assumptions, None


# ------------------------------------------------------------------ calculators

def front_open_space(facts: Facts) -> Calculation | Missing | NotApplicable:
    """Rule 56(1)(a)."""
    if facts.road_width_m is None:
        return Missing("road width")
    assumptions, refusal = _low_rise_assumptions(facts)
    if refusal:
        return refusal
    result, band = _band(facts.road_width_m, t.FRONT_OPEN_SPACE, "m")
    return Calculation(
        code="RESIDENTIAL_FRONT_OPEN_SPACE", rule="56(1)(a)", pdf_page=t.FRONT_OPEN_SPACE_PAGE,
        inputs=[_measure("road width", facts.road_width_m, "m")],
        matched_band=f"street width {band}",
        formula="Table in Rule 56(1)(a): width of street facing the plot -> minimum front open space",
        result={"name": "minimum front open space", "value": _num(result, "0.1"), "unit": "m"},
        assumptions=assumptions,
    )


def rear_open_space(facts: Facts) -> Calculation | Missing | NotApplicable:
    """Rule 56(2)(a)."""
    if facts.plot_area_sqm is None:
        return Missing("plot area")
    assumptions, refusal = _low_rise_assumptions(facts)
    if refusal:
        return refusal
    result, band = _band(facts.plot_area_sqm, t.REAR_OPEN_SPACE, "sq m")
    return Calculation(
        code="RESIDENTIAL_REAR_OPEN_SPACE", rule="56(2)(a)", pdf_page=t.REAR_OPEN_SPACE_PAGE,
        inputs=[_measure("plot area", facts.plot_area_sqm, "sq m")],
        matched_band=f"plot area {band}",
        formula="Table in Rule 56(2)(a): plot area -> minimum rear open space",
        result={"name": "minimum rear open space", "value": _num(result), "unit": "m",
                "text": "Nil" if result == 0 else f"{_num(result)} m"},
        assumptions=assumptions,
    )


def side_open_space(facts: Facts) -> Calculation | Missing | NotApplicable:
    """Rule 56(3): side open spaces for low-rise residential buildings."""
    assumptions, refusal = _low_rise_assumptions(facts)
    if refusal:
        return refusal
    if facts.building_form == "unknown":
        return Missing("building form (detached, semi-detached or row)")

    if facts.building_form == "row":
        value, sides, band = Decimal("0"), "both sides", "row building"
    elif facts.building_form == "semi_detached":
        value, sides, band = Decimal("3"), "one side", "semi-detached building"
        if (facts.building_height_m is not None and facts.building_height_m <= Decimal("10")
                and facts.plot_frontage_m is not None and facts.plot_frontage_m <= Decimal("10")):
            value, band = Decimal("2.5"), "semi-detached; height up to 10 m and frontage up to 10 m"
    elif facts.building_form == "detached":
        value, sides, band = Decimal("3"), "both sides", "detached building"
        if (facts.building_height_m is not None and facts.building_height_m <= Decimal("7")
                and facts.plot_frontage_m is not None and facts.plot_frontage_m < Decimal("12")):
            value, sides, band = Decimal("1.5"), "one side", "detached; height up to 7 m and frontage below 12 m"
    else:
        return NotApplicable("Unsupported building form for Rule 56(3).")

    inputs = [{"name": "building form", "value": facts.building_form, "unit": ""}]
    if facts.building_height_m is not None:
        inputs.append(_measure("building height", facts.building_height_m, "m"))
    if facts.plot_frontage_m is not None:
        inputs.append(_measure("plot frontage", facts.plot_frontage_m, "m"))
    return Calculation(
        code="RESIDENTIAL_SIDE_OPEN_SPACE", rule="56(3)", pdf_page=t.REAR_OPEN_SPACE_PAGE,
        inputs=inputs, matched_band=band,
        formula="Rule 56(3): building form, height and plot frontage -> minimum side open space",
        result={"name": "minimum side open space", "value": _num(value, "0.1"),
                "unit": "m", "requiredOn": sides, "text": "Nil" if value == 0 else f"{_num(value)} m"},
        assumptions=assumptions,
    )


def floor_area(facts: Facts) -> Calculation | Missing | NotApplicable:
    """Rule 61 Table 8: maximum total built-up area = plot area x FAR, plus an illustrative floor count."""
    if (facts.building_height_m is not None and facts.building_height_m > t.LOW_RISE_HEIGHT_LIMIT_M
            or facts.storeys is not None and facts.storeys > 10):
        return rule42_planning(facts)
    if facts.plot_area_sqm is None:
        return Missing("plot area")
    use = facts.building_use
    assumptions = []
    if use in ("residential", "unknown", "shop_cum_residence"):
        far, category = t.FAR_RESIDENTIAL, "Residential"
        if use == "unknown":
            assumptions.append("Assumes residential use (FAR 1.25).")
        if use == "shop_cum_residence":
            assumptions.append("Table 8 has no shop-cum-residence row; the residential FAR is shown.")
    elif use == "public":
        far, category = t.FAR_PUBLIC_SEMI_PUBLIC, "Public / semi-public"
    elif use == "commercial":
        if facts.commercial_category not in t.FAR_COMMERCIAL:
            return Missing("commercial category (city centre, sub-city centre, community centre, "
                           "local or convenience shopping centre)")
        far = t.FAR_COMMERCIAL[facts.commercial_category]
        category = "Commercial - " + facts.commercial_category.replace("_", " ")
    else:
        return NotApplicable(f"Table 8 does not give a FAR for '{use}' use (industrial follows Rule 48).")

    max_built_up = facts.plot_area_sqm * far
    notes = ["Rule 61: F.A.R and density may be adopted as provided in the Development Plan of the town; "
             "check the Bhopal Development Plan."]
    if category == "Residential":
        notes.append("Rule 56 open-space tables apply to residential buildings up to 12.5 m high; "
                     "taller buildings follow Rule 42.")

    # Illustration only: the rules do not set ground coverage for these plots.
    coverages = ([facts.ground_coverage_percent] if facts.ground_coverage_percent
                 else list(ILLUSTRATIVE_COVERAGE_PERCENT))
    illustration = []
    for percent in coverages:
        footprint = facts.plot_area_sqm * percent / Decimal(100)
        floors = max_built_up / footprint
        illustration.append({
            "groundCoveragePercent": _num(percent, "1"),
            "footprintSqm": _num(footprint),
            "floors": _num(floors, "0.1"),
        })

    return Calculation(
        code="MAX_BUILT_UP_AREA_FAR", rule="61 (Table 8)", pdf_page=t.FAR_PAGE,
        inputs=[_measure("plot area", facts.plot_area_sqm, "sq m"),
                {"name": "FAR", "value": _num(far), "unit": "", "category": category}],
        matched_band=category,
        formula=f"maximum built-up area = plot area x FAR = {_num(facts.plot_area_sqm)} x {_num(far)}",
        result={
            "name": "maximum total built-up area (all floors)", "value": _num(max_built_up), "unit": "sq m",
            "floorIllustration": illustration,
            "floorIllustrationLabel": ("ILLUSTRATION ONLY, NOT FROM THE RULES: floors = maximum built-up "
                                       "area / ground footprint. These rules do not set ground coverage "
                                       "for this plot; the Development Plan may."),
        },
        assumptions=assumptions,
        notes=notes,
    )


def _is_high_rise(facts: Facts) -> bool | None:
    """Rule 2(38): more than ten storeys, or more than 30 m. None when unknown."""
    if facts.storeys is not None and facts.storeys > t.HIGH_RISE_MIN_STOREYS_EXCLUSIVE:
        return True
    if facts.building_height_m is not None and facts.building_height_m > t.HIGH_RISE_MIN_HEIGHT_EXCLUSIVE_M:
        return True
    if facts.storeys is None and facts.building_height_m is None:
        return None
    return False


def building_permission_fee(facts: Facts) -> Calculation | Missing | NotApplicable:
    """Rule 21(3)(b) (other than high rise) or 21(3)(c) (high rise)."""
    assumptions = []
    built_up = _built_up_or_far(facts, assumptions)
    if built_up is None:
        return Missing("built-up area")

    use = facts.building_use
    if use == "unknown":
        use = "residential"
        assumptions.append("Assumes a building used exclusively as residence.")
    high_rise = _is_high_rise(facts)
    if high_rise is None:
        high_rise = False
        assumptions.append("Assumes not a high rise building (Rule 2(38): up to 10 storeys and up to 30 m).")

    if high_rise:
        base = built_up * t.HIGH_RISE_FEE_PER_SQM
        surcharge = t.HIGH_RISE_FEE_SURCHARGE.get(use, Decimal(0))
        rule, page = "21(3)(c)", t.HIGH_RISE_FEE_PAGE
        band = "high rise building (Rule 2(38))"
        formula = f"Rs. 10.00 x {_num(built_up)} sq m"
    else:
        base, band_text = _band(built_up, t.RESIDENTIAL_FEE_BANDS, "sq m")
        surcharge = t.LOW_RISE_FEE_SURCHARGE.get(use, Decimal(0))
        rule, page = "21(3)(b)", t.FEE_PAGE
        band = f"built-up area {band_text}"
        formula = f"item 1 band for {_num(built_up)} sq m = {rupees(base)}"
    fee = base * (1 + surcharge)
    if surcharge:
        formula += f" + {_num(surcharge * 100, '1')}% for {use.replace('_', ' ')}"

    return Calculation(
        code="BUILDING_PERMISSION_FEE", rule=rule, pdf_page=page,
        inputs=[_measure("built-up area", built_up, "sq m"),
                {"name": "building use", "value": use, "unit": ""},
                {"name": "high rise", "value": high_rise, "unit": ""}],
        matched_band=band, formula=formula,
        result={"name": "building permission fee", "value": _num(fee, "1"), "unit": "Rs.", "text": rupees(fee)},
        assumptions=assumptions,
        notes=["Application fee (Rule 21(2)(ii)) is charged separately."],
    )


def application_fee(facts: Facts) -> Calculation | Missing | NotApplicable:
    """Rule 21(2)(ii): Rs 1.00 per sq m of the proposed built-up area."""
    assumptions: list[str] = []
    built_up = _built_up_or_far(facts, assumptions)
    if built_up is None:
        return Missing("built-up area")
    fee = built_up * t.APPLICATION_FEE_PER_SQM
    return Calculation(
        code="BUILDING_PERMISSION_APPLICATION_FEE", rule="21(2)(ii)", pdf_page=t.APPLICATION_FEE_PAGE,
        inputs=[_measure("built-up area", built_up, "sq m")],
        formula=f"Rs. 1.00 x {_num(built_up)} sq m",
        result={"name": "application fee", "value": _num(fee), "unit": "Rs.", "text": rupees(fee)},
        assumptions=assumptions,
        notes=["Application fee is not refunded in any case (Rule 21(2)(iii))."],
    )


def _built_up_or_far(facts: Facts, assumptions: list[str]) -> Decimal | None:
    """The stated built-up area, else the maximum under FAR (counter questions often give only the plot)."""
    if facts.built_up_area_sqm is not None:
        return facts.built_up_area_sqm
    if facts.plot_area_sqm is None:
        return None
    far_result = floor_area(facts)
    if not isinstance(far_result, Calculation):
        return None
    amount=far_result.result.get("value",far_result.result.get("maximumBuiltUpSqm"))
    if amount is None:
        return None
    built_up = Decimal(str(amount))
    assumptions.append(f"Built-up area not given: uses the maximum built-up area under FAR "
                       f"({_num(built_up)} sq m). The actual fee depends on the built-up area applied for.")
    return built_up


def farm_house(facts: Facts) -> Calculation | Missing | NotApplicable:
    """Rule 17: minimum plot, maximum FAR and built-up area for a farm house."""
    if facts.plot_area_sqm is None:
        return Calculation(code="FARM_HOUSE_LIMITS",rule="17",pdf_page=t.FARM_HOUSE_PAGE,inputs=[],
            formula="Rule 17(a)-(f): minimum plot and maximum FAR; no site-specific built-up area computed",
            result={"minimumPlotHa":0.40,"maximumFar":0.10,"maxHeightM":7.5,"minRoadWidthM":7.5,
                    "minOpenSpaceAllSidesM":7.5},
            notes=["Agricultural land designation, sloping roof and at least 100 live trees per hectare are required."])
    hectares = facts.plot_area_sqm / t.SQM_PER_HECTARE
    eligible = hectares >= t.FARM_HOUSE_MIN_PLOT_HA
    max_built_up = facts.plot_area_sqm * t.FARM_HOUSE_MAX_FAR
    return Calculation(
        code="FARM_HOUSE_LIMITS", rule="17", pdf_page=t.FARM_HOUSE_PAGE,
        inputs=[_measure("plot area", facts.plot_area_sqm, "sq m"),
                _measure("plot area", hectares, "ha")],
        matched_band="meets minimum plot size 0.40 ha" if eligible else "below minimum plot size 0.40 ha",
        formula=f"maximum built-up area = plot area x 0.10 = {_num(facts.plot_area_sqm)} x 0.10",
        result={"name": "maximum built-up area", "value": _num(max_built_up), "unit": "sq m",
                "meetsMinimumPlot": eligible,
                "maxHeightM": _num(t.FARM_HOUSE_MAX_HEIGHT_M, "0.1"),
                "minRoadWidthM": _num(t.FARM_HOUSE_MIN_ROAD_WIDTH_M, "0.1"),
                "minOpenSpaceAllSidesM": _num(t.FARM_HOUSE_MIN_OPEN_SPACE_M)},
        notes=[] if eligible else ["A farm house needs a plot of at least 0.40 hectare (Rule 17(a))."],
    )


def height_classification(facts: Facts) -> Calculation | Missing | NotApplicable:
    """Rule 2(38): classifies a stated height/storey count; it does not invent a permitted height."""
    if facts.building_height_m is None and facts.storeys is None:
        return Missing("building height or number of storeys")
    high_rise = _is_high_rise(facts)
    inputs = []
    if facts.building_height_m is not None:
        inputs.append(_measure("building height", facts.building_height_m, "m"))
    if facts.storeys is not None:
        inputs.append({"name": "storeys", "value": facts.storeys, "unit": ""})
    return Calculation(
        code="HIGH_RISE_CLASSIFICATION", rule="2(38)", pdf_page=t.HIGH_RISE_DEFINITION_PAGE,
        inputs=inputs,
        matched_band="high rise" if high_rise else "not high rise under Rule 2(38)",
        formula="high rise when height is above 30 m or the building has more than 10 storeys",
        result={"name": "high rise", "value": bool(high_rise), "unit": ""},
        notes=["This classifies the proposal; it does not determine the maximum permitted height."],
    )


def electric_line_clearance(facts: Facts) -> Calculation | Missing | NotApplicable:
    """Rule 52 minimum building/balcony clearance from overhead electric lines."""
    category = facts.electric_line_category
    if category == "unknown":
        return Missing("electric line voltage category")
    if category == "low_medium":
        result = {"verticalM": 2.5, "horizontalM": 1.2}
        band = "low/medium voltage or service line"
    elif category == "high_up_to_33000":
        result = {"verticalM": 3.7, "horizontalM": 1.2}
        band = "high voltage up to and including 33000 volts"
    elif category == "extra_high_above_33000":
        result = {"horizontalCorridorM": 15.0, "openEachSideOfCentreLineM": 7.5}
        band = "extra high voltage above 33000 volts"
    else:
        return NotApplicable("Unsupported electric line category.")
    return Calculation(
        code="ELECTRIC_LINE_CLEARANCE", rule="52", pdf_page=t.ELECTRIC_CLEARANCE_PAGE,
        inputs=[{"name": "electric line category", "value": category, "unit": ""}],
        matched_band=band,
        formula="Rule 52 table; use the applicable Electricity Rules value when it is greater",
        result={"name": "minimum clearance", **result},
        notes=["Measure vertical clearance from maximum sag and horizontal clearance from maximum wind deflection."],
    )


def sanction_validity(facts: Facts) -> Calculation | Missing | NotApplicable:
    """Rule 23 duration and revalidation limits."""
    return Calculation(
        code="SANCTION_VALIDITY", rule="23", pdf_page=t.SANCTION_VALIDITY_PAGE,
        inputs=[], matched_band="development permission under Section 30",
        formula="3 years + up to two consecutive revalidation terms of 1 year each",
        result={"initialYears": 3, "renewalTerms": 2, "yearsPerRenewal": 1, "maximumYears": 5},
        notes=["If execution begins during validity, Rule 23(3) says the permission does not lapse thereafter."],
    )


# Which calculators each topic brings in (topic codes match the query-intent service).
def rule42_planning(facts: Facts) -> Calculation | Missing | NotApplicable:
    """Evaluate every Table 4/5 row; all three site minima must hold together."""
    high_rise = (facts.building_height_m is not None and facts.building_height_m > Decimal("30")
                 or facts.storeys is not None and facts.storeys > 10)
    if facts.building_height_m is not None and facts.building_height_m <= t.LOW_RISE_HEIGHT_LIMIT_M and not high_rise:
        return NotApplicable("Rule 42 tables apply above 12.5 m or to high rise buildings under Rule 2(38).")
    if facts.building_height_m is not None and not high_rise and facts.storeys is None:
        return Missing("storey count (more than 10 storeys uses Rule 42 Table 5 even at 30 m or below)")
    for label, field_value in (("road width", facts.road_width_m),
                               ("plot area", facts.plot_area_sqm),
                               ("plot frontage", facts.plot_frontage_m)):
        if field_value is None:
            return Missing(label)
    table = 5 if high_rise else 4 if facts.building_height_m is not None else "4/5"
    specs = ([(4,i,row) for i,row in enumerate(t.RULE42_TABLE4,1)] if table in (4,"4/5") else [])
    specs += ([(5,i,row) for i,row in enumerate(t.RULE42_TABLE5,1)] if table in (5,"4/5") else [])
    evaluated = []
    for row_table,index,(road, area, frontage, far, height, front, sides, coverage) in specs:
        site_eligible = (facts.road_width_m >= road and facts.plot_area_sqm >= area
                         and facts.plot_frontage_m >= frontage)
        height_eligible = (None if facts.building_height_m is None else
                          facts.building_height_m > Decimal("90") if height is None else facts.building_height_m <= height)
        evaluated.append({
            "table":row_table,"row": index, "minimumRoadM": _num(road), "minimumPlotSqm": _num(area),
            "minimumFrontageM": _num(frontage), "far": _num(far),
            "heightUpToM": None if height is None else _num(height),
            "heightBand": "above 90 m; no numerical upper limit in this row" if height is None else f"up to {height} m",
            "frontMosM": _num(front), "sideRearMosM": _num(sides),
            "groundCoveragePercent": None if coverage is None else _num(coverage),
            "siteEligible": site_eligible, "proposedHeightEligible": height_eligible,
            "eligible": site_eligible and height_eligible is not False,
        })
    eligible = [row for row in evaluated if row["eligible"]]
    # Prefer the least restrictive height row containing the proposal. Do not combine columns across rows.
    selected = eligible[0] if eligible and facts.building_height_m is not None else None
    site_rows = [row for row in evaluated if row["siteEligible"]]
    maximum = site_rows[-1]["heightUpToM"] if site_rows else None
    result = {
        "table": table, "rows": evaluated, "eligibleRows":eligible,
        "compliantWithTable": bool(eligible) if facts.building_height_m is not None else None,
        "selectedRow": selected, "maximumTableHeightM": maximum,
        "above90mBandAvailable": table in (5,"4/5") and bool(site_rows) and site_rows[-1]["row"] == 5,
        "conditionalCommercialFar": ([
            {"rows": [4, 5], "maximumFar": 5, "conditions": "Commercial use in planning areas; authority approval; TDR and Premium FAR"},
            {"rows": [5], "maximumFar": 7, "conditions": "Commercial use in Development Plan CBD/TOD corridor; authority approval; TDR and Premium FAR"},
        ] if table in (5,"4/5") else []),
    }
    if selected:
        result["maximumBuiltUpSqm"] = _num(facts.plot_area_sqm * Decimal(str(selected["far"])))
    return Calculation(
        code="RULE42_PLANNING_NORMS", rule="42" if table=="4/5" else f"42({2 if high_rise else 1})", pdf_page=59 if high_rise else 58,
        inputs=[_measure("road width", facts.road_width_m, "m"),
                _measure("plot area", facts.plot_area_sqm, "sq m"),
                _measure("plot frontage", facts.plot_frontage_m, "m")]
                + ([_measure("building height",facts.building_height_m,"m")] if facts.building_height_m is not None else []),
        matched_band=f"Table {table}",
        formula="A single row must satisfy road >= minimum, plot >= minimum, frontage >= minimum and proposed height band; plot area x row FAR",
        result=result,
        notes=["Table 4 ground coverage column and note were deleted on 6 March 2025; do not infer a coverage cap.",
               "Table 5 row 5 means above 90 m; absence of a numerical cap is not unlimited permission.",
               "Rule 42(3)-(6): NBC Part IV fire measures, high-rise site clearance/planning/building permissions, motorable unobstructed fire access and entrance/overhead clearance at least 4.5 m.",
               "Development Plan and competent authority conditions still apply; premium FAR is never automatically granted."],
    )


TOPIC_CALCULATORS = {
    "SETBACK": (front_open_space, rear_open_space, side_open_space),
    "SETBACK_FRONT": (front_open_space,),
    "SETBACK_REAR": (rear_open_space,),
    "SETBACK_SIDE": (side_open_space,),
    "FAR": (floor_area,),
    "HEIGHT": (height_classification, rule42_planning),
    "FEES": (building_permission_fee, application_fee),
    "FARM_HOUSE": (farm_house,),
    "ELECTRIC_LINES": (electric_line_clearance,),
    "VALIDITY": (sanction_validity,),
}


def run(facts: Facts, topics: list[str]) -> dict[str, Any]:
    """Runs every calculator the topics call for. Missing inputs and reasons are reported, not guessed."""
    chosen = []
    for topic in topics:
        for calculator in TOPIC_CALCULATORS.get(topic, ()):
            if calculator not in chosen:
                chosen.append(calculator)
    if "FARM_HOUSE" in topics and floor_area in chosen:
        chosen.remove(floor_area)  # Rule 17 FAR applies, not Table 8
    if (facts.building_height_m is not None and facts.building_height_m > t.LOW_RISE_HEIGHT_LIMIT_M
            or facts.storeys is not None and facts.storeys > 10):
        if any(topic.startswith("SETBACK") for topic in topics) or "FAR" in topics:
            chosen = [fn for fn in chosen if fn not in (front_open_space, rear_open_space, side_open_space, floor_area)]
            if rule42_planning not in chosen:
                chosen.append(rule42_planning)

    calculations, missing, not_applicable = [], [], []
    for calculator in chosen:
        outcome = calculator(facts)
        if isinstance(outcome, Calculation):
            calculations.append(outcome.to_contract())
        elif isinstance(outcome, NotApplicable):
            not_applicable.append(outcome.reason)
        elif outcome.input not in missing:
            missing.append(outcome.input)
    return {"calculations": calculations, "missingInputs": missing, "notApplicable": not_applicable}


# ------------------------------------------------------------------ request parsing

AREA_UNITS = ("sqm", "sqft", "sqyd", "ha")
LENGTH_UNITS = ("m", "ft")
BUILDING_USES = ("residential", "commercial", "shop_cum_residence", "industrial", "public",
                 "agricultural", "other", "unknown")
ELECTRIC_LINE_CATEGORIES = ("low_medium", "high_up_to_33000", "extra_high_above_33000", "unknown")
BUILDING_FORMS = ("detached", "semi_detached", "row", "unknown")


def facts_from_contract(body: dict[str, Any]) -> Facts:
    """Builds Facts from the camelCase contract, e.g. {"plotArea": {"value": 200, "unit": "sqm"}}."""
    use = body.get("buildingUse") or "unknown"
    if use not in BUILDING_USES:
        raise ValueError(f"buildingUse must be one of {', '.join(BUILDING_USES)}")
    storeys = body.get("storeys")
    if storeys is not None and (isinstance(storeys, bool) or not isinstance(storeys, int) or storeys < 1):
        raise ValueError("storeys must be a positive whole number")
    category = body.get("commercialCategory")
    if category is not None and category not in t.FAR_COMMERCIAL:
        raise ValueError(f"commercialCategory must be one of {', '.join(t.FAR_COMMERCIAL)}")
    coverage = _positive(body.get("groundCoveragePercent"), "groundCoveragePercent")
    if coverage is not None and coverage > 100:
        raise ValueError("groundCoveragePercent must be at most 100")
    electric_category = body.get("electricLineCategory") or "unknown"
    if electric_category not in ELECTRIC_LINE_CATEGORIES:
        raise ValueError("electricLineCategory is invalid")
    building_form = body.get("buildingForm") or "unknown"
    if building_form not in BUILDING_FORMS:
        raise ValueError("buildingForm is invalid")
    return Facts(
        plot_area_sqm=_area(body.get("plotArea"), "plotArea"),
        road_width_m=_length(body.get("roadWidth"), "roadWidth"),
        plot_frontage_m=_length(body.get("plotFrontage"), "plotFrontage"),
        building_height_m=_length(body.get("buildingHeight"), "buildingHeight"),
        built_up_area_sqm=_area(body.get("builtUpArea"), "builtUpArea"),
        storeys=storeys,
        building_use=use,
        commercial_category=category,
        ground_coverage_percent=coverage,
        electric_line_category=electric_category,
        building_form=building_form,
    )


def _area(measure: Any, name: str) -> Decimal | None:
    value, unit = _value_and_unit(measure, name, AREA_UNITS)
    return None if value is None else area_to_sqm(value, unit)


def _length(measure: Any, name: str) -> Decimal | None:
    value, unit = _value_and_unit(measure, name, LENGTH_UNITS)
    return None if value is None else length_to_m(value, unit)


def _value_and_unit(measure: Any, name: str, units: tuple[str, ...]) -> tuple[Decimal | None, str]:
    if measure is None:
        return None, ""
    if not isinstance(measure, dict) or measure.get("unit") not in units:
        raise ValueError(f"{name} must be {{value, unit}} with unit one of {', '.join(units)}")
    return _positive(measure.get("value"), name), measure["unit"]


def _positive(value: Any, name: str) -> Decimal | None:
    if value is None:
        return None
    if isinstance(value, bool) or not isinstance(value, (int, float)) or value <= 0:
        raise ValueError(f"{name} must be a positive number")
    return Decimal(str(value))

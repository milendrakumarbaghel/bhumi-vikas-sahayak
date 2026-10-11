"""Table values from the M.P. Bhumi Vikas Rules, 2012 (consolidated to 6 March 2025).

Every value here was read from the official English PDF
(track-1-bhumi-vikas-sahayak/resources/MP_Bhumi_Vikas_Rules_2012_upto_2025-03-06_EN.pdf)
on the page given in the comment, and matches the rules JSONL text. Change a value only
after checking the PDF page again.
"""
from __future__ import annotations

from decimal import Decimal as D

# Rule 56(1)(a), PDF page 77 - front open space for residential buildings up to 12.5 m high.
# (street width up to and including, front open space). None = no upper limit.
FRONT_OPEN_SPACE = (
    (D("9.0"), D("3.0")),    # Up to 9.0 meters                          3.0 meters
    (D("12"), D("3.6")),     # More than 9.0 meters and up to 12 meters  3.6 meters
    (D("18"), D("4.5")),     # More than 12.0 meters and up to 18 meters 4.5 meters
    (None, D("6.0")),        # Above 18 meters                           6.0 meters
)
FRONT_OPEN_SPACE_PAGE = 77

# Rule 56(2)(a), PDF page 78 - rear open space for residential buildings up to 12.5 m high.
# (plot area up to and including in sq m, rear open space in m; 0 means "Nil").
REAR_OPEN_SPACE = (
    (D("40.00"), D("0")),     # Up to 40.00                    Nil
    (D("150.00"), D("1.50")), # Above 40.00 and up to 150.00   1.50
    (D("225.00"), D("2.50")), # Above 150 and up to 225.00     2.50
    (None, D("3.00")),        # Above 225.00                   3.00
)
REAR_OPEN_SPACE_PAGE = 78

# Rules 56(1)-(2) apply to residential buildings "having height up to 12.5 meters" (PDF p.77-78);
# above that, Rule 56(5) points to Tables 4 and 5 of Rule 42.
LOW_RISE_HEIGHT_LIMIT_M = D("12.5")

# Rule 21(3)(b) item 1, PDF page 36 - building permission fee, building used exclusively as
# residence (other than high rise). The printed bands are whole numbers with gaps
# (0-75, 76-125, ...). Decision (team, 11 Oct 2026): each band runs up to and including its
# upper figure, and anything above it falls in the next band, so 75.5 sq m pays Rs. 1,250.
# (built-up area up to and including in sq m, fee in Rs.)
RESIDENTIAL_FEE_BANDS = (
    (D("75"), D("750")),
    (D("125"), D("1250")),
    (D("200"), D("2000")),
    (D("300"), D("3000")),
    (D("400"), D("4000")),
    (D("600"), D("6000")),
    (D("750"), D("7500")),
    (D("1000"), D("10000")),
    (D("1250"), D("12500")),
    (D("1500"), D("15000")),
    (D("2000"), D("20000")),
    (D("2500"), D("25000")),
    (None, D("50000")),       # above 2500
)
FEE_PAGE = 36

# Rule 21(3)(b) items 2, 4 (PDF p.36): shops / commercial / industrial and shop-cum-residence
# pay the item 1 fee plus 50%. Items 3 and 6: same as item 1.
LOW_RISE_FEE_SURCHARGE = {
    "commercial": D("0.50"),
    "industrial": D("0.50"),
    "shop_cum_residence": D("0.50"),
}

# Rule 21(3)(c), PDF page 37 - high rise buildings: Rs. 10.00 per sq m of built-up area for
# residence; commercial/industrial +100%; shop-cum-residence +50%; others as item 1.
HIGH_RISE_FEE_PER_SQM = D("10.00")
HIGH_RISE_FEE_SURCHARGE = {
    "commercial": D("1.00"),
    "industrial": D("1.00"),
    "shop_cum_residence": D("0.50"),
}
HIGH_RISE_FEE_PAGE = 37

# Rule 2(38), PDF page 8 - "high rise building" means a building (a) which consists of more
# than ten stories; or (b) which has a height of more than 30 meters.
HIGH_RISE_MIN_STOREYS_EXCLUSIVE = 10
HIGH_RISE_MIN_HEIGHT_EXCLUSIVE_M = D("30")
HIGH_RISE_DEFINITION_PAGE = 8

# Rule 21(2)(ii), PDF page 35 - application fee for building permission:
# Rs 1.00 per square meter of the proposed built-up area.
APPLICATION_FEE_PER_SQM = D("1.00")
APPLICATION_FEE_PAGE = 35

# Rule 61, Table 8, PDF page 83 - Floor Area Ratio. "*F.A.R and Density may be adopted as
# provided in the Development Plans of respective towns."
FAR_RESIDENTIAL = D("1.25")
FAR_PUBLIC_SEMI_PUBLIC = D("1.00")
FAR_COMMERCIAL = {
    "city_centre": D("2.50"),
    "sub_city_centre": D("2.00"),
    "community_centre": D("1.75"),
    "local_shopping_centre": D("1.50"),
    "convenience_shopping_centre": D("1.25"),
}
FAR_PAGE = 83

# Rule 17, PDF page 34 - farm house in an area designated for agricultural use.
FARM_HOUSE_MIN_PLOT_HA = D("0.40")
FARM_HOUSE_MAX_FAR = D("0.10")
FARM_HOUSE_MAX_HEIGHT_M = D("7.5")
FARM_HOUSE_MIN_ROAD_WIDTH_M = D("7.5")
FARM_HOUSE_MIN_OPEN_SPACE_M = D("7.50")
FARM_HOUSE_PAGE = 34

SQM_PER_HECTARE = D("10000")

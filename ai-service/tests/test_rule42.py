from decimal import Decimal as D
import unittest
from ai_service.calculators import calculators as c


class Rule42Test(unittest.TestCase):
    def test_all_eight_official_table_rows(self):
        rows = [(12,1000,18,18,1.5,7.5,6,None),(18,1500,21,24,1.75,9,6,None),
                (24,2000,30,30,2,12,7.5,None),(30,2500,30,45,2.25,15,7.5,30),
                (36,3000,40,60,2.5,18,9,30),(45,3500,45,75,2.75,21,9,30),
                (60,4000,50,90,3,24,10,30),(75,4500,60,91,3,30,12,30)]
        for road,plot,frontage,height,far,front,side,coverage in rows:
            with self.subTest(height=height):
                result=c.rule42_planning(c.Facts(road_width_m=D(road),plot_area_sqm=D(plot),
                    plot_frontage_m=D(frontage),building_height_m=D(height),storeys=8))
                self.assertIsInstance(result,c.Calculation)
                selected=result.result['selectedRow']
                self.assertTrue(result.result['compliantWithTable'])
                self.assertEqual(far,selected['far'])
                self.assertEqual(front,selected['frontMosM'])
                self.assertEqual(side,selected['sideRearMosM'])
                self.assertEqual(coverage,selected['groundCoveragePercent'])
                self.assertEqual(plot*far,result.result['maximumBuiltUpSqm'])

    def test_site_minima_are_conjunctive_and_not_mixed_across_rows(self):
        for field,value in [('road_width_m',D('11.99')),('plot_area_sqm',D('999.99')),
                            ('plot_frontage_m',D('17.99'))]:
            inputs=dict(road_width_m=D(12),plot_area_sqm=D(1000),plot_frontage_m=D(18),building_height_m=D(18),storeys=8)
            inputs[field]=value
            result=c.rule42_planning(c.Facts(**inputs))
            self.assertFalse(result.result['compliantWithTable'])
            self.assertIsNone(result.result['selectedRow'])

    def test_more_than_ten_storeys_uses_high_rise_table_even_below_30m(self):
        result=c.rule42_planning(c.Facts(road_width_m=D(30),plot_area_sqm=D(2500),
            plot_frontage_m=D(30),building_height_m=D(29),storeys=11))
        self.assertEqual(5,result.result['table'])
        self.assertEqual(2.25,result.result['selectedRow']['far'])

    def test_above_90m_never_claims_unlimited_permission_or_automatic_premium_far(self):
        result=c.rule42_planning(c.Facts(road_width_m=D(75),plot_area_sqm=D(4500),
            plot_frontage_m=D(60),building_height_m=D(120),building_use='commercial'))
        self.assertTrue(result.result['above90mBandAvailable'])
        self.assertIsNone(result.result['maximumTableHeightM'])
        self.assertEqual(3,result.result['selectedRow']['far'])
        self.assertIn('authority approval',result.result['conditionalCommercialFar'][0]['conditions'])

    def test_setback_and_far_route_to_rule42_for_tall_buildings(self):
        result=c.run(c.Facts(road_width_m=D(24),plot_area_sqm=D(2000),plot_frontage_m=D(30),
                    building_height_m=D(30),storeys=8),['SETBACK','FAR'])
        self.assertEqual(['RULE42_PLANNING_NORMS'],[row['code'] for row in result['calculations']])

    def test_unknown_height_returns_catalog_not_an_invented_approval(self):
        result=c.rule42_planning(c.Facts(road_width_m=D(75),plot_area_sqm=D(4500),plot_frontage_m=D(60)))
        self.assertEqual(8,len(result.result['rows']))
        self.assertIsNone(result.result['selectedRow'])
        self.assertIsNone(result.result['compliantWithTable'])

    def test_storey_count_is_required_to_distinguish_table4_from_high_rise(self):
        result=c.rule42_planning(c.Facts(road_width_m=D(24),plot_area_sqm=D(2000),
            plot_frontage_m=D(30),building_height_m=D(30)))
        self.assertIsInstance(result,c.Missing)
        self.assertIn('storey count',result.input)

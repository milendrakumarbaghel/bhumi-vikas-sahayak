package org.springboot.aiservice.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springboot.aiservice.llm.LlmClient;
import tools.jackson.databind.json.JsonMapper;

class QueryIntentServiceTest {

    @Test
    void removesMeasurementsThatWereNotPresentInTheQuestion() {
        QueryIntentService service = new QueryIntentService(new FixedLlm(), JsonMapper.builder().build());

        QueryIntentService.Analysis result = service.analyse("My plot is 200 sqm on a 9 m road");

        assertThat(result.plotArea().value()).isEqualTo(200.0);
        assertThat(result.roadWidth().value()).isEqualTo(9.0);
        assertThat(result.buildingHeight()).isNull();
        assertThat(result.model()).isEqualTo("test-model");
    }

    private static class FixedLlm implements LlmClient {
        public boolean isConfigured() { return true; }
        public String modelName() { return "test-model"; }
        public String completeJson(String system, String user, Map<String, Object> schema, Effort effort) {
            return """
                    {
                      "language": "en",
                      "english_question": "My plot is 200 sqm on a 9 m road",
                      "search_terms": "plot road open space",
                      "topics": ["SETBACK"],
                      "plot_area": {"value": 200, "unit": "sqm"},
                      "road_width": {"value": 9, "unit": "m"},
                      "building_height": {"value": 99, "unit": "m"},
                      "built_up_area": null,
                      "building_use": "unknown",
                      "rule_refs": []
                    }
                    """;
        }
    }
}

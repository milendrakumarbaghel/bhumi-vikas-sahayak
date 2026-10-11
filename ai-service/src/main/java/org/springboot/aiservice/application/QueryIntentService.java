package org.springboot.aiservice.application;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springboot.aiservice.llm.LlmClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Uses an LLM only to propose language, translation, topics and raw facts. */
@Service
public class QueryIntentService {

    public static final List<String> TOPICS = List.of(
            "SETBACK", "FAR", "HEIGHT", "FEES", "PARKING", "FARM_HOUSE", "VALIDITY",
            "DEEMED_SANCTION", "ELECTRIC_LINES", "PLAN_SIGNING", "APPLICATION_DOCUMENTS",
            "PLAN_CHANGE", "LIFTS", "DEVELOPMENT_PLAN", "DEVELOPMENT_PERMISSION", "PLOT_SIZE",
            "BALCONY_PROJECTIONS", "BASEMENT", "FIRE");

    private static final String SYSTEM_PROMPT = """
            You prepare search input for a tool that answers questions only from the Madhya Pradesh
            Bhumi Vikas Rules, 2012. Do not answer the legal question and do not add facts.

            Return the user's language (en/hi), a faithful English translation, 5-15 English legal
            search terms, applicable topic codes, explicitly stated measurements, building use, and
            explicitly named rule references. Copy measurement numbers and units exactly; use null
            when absent. Hindi, Devanagari and romanised Hindi may occur. "gaj" means sqyd.

            Topic codes: %s
            """.formatted(String.join(", ", TOPICS));

    private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");
    private static final Map<String, Object> SCHEMA = buildSchema();

    private final LlmClient llm;
    private final JsonMapper json;

    public QueryIntentService(LlmClient llm, JsonMapper json) {
        this.llm = llm;
        this.json = json;
    }

    public Analysis analyse(String question) {
        String normalized = Normalizer.normalize(question, Normalizer.Form.NFC).strip();
        String reply = llm.completeJson(
                SYSTEM_PROMPT,
                "Question:\n" + normalized,
                SCHEMA,
                LlmClient.Effort.low);
        JsonNode node = json.readTree(reply);
        Set<Double> stated = numbersIn(normalized);

        return new Analysis(
                node.path("language").asString("en"),
                node.path("english_question").asString(normalized),
                node.path("search_terms").asString(""),
                strings(node.path("topics")),
                verifiedMeasure(node.path("plot_area"), stated),
                verifiedMeasure(node.path("road_width"), stated),
                verifiedMeasure(node.path("building_height"), stated),
                verifiedMeasure(node.path("built_up_area"), stated),
                node.path("building_use").asString("unknown"),
                strings(node.path("rule_refs")),
                llm.modelName());
    }

    private static List<String> strings(JsonNode node) {
        List<String> values = new ArrayList<>();
        node.forEach(item -> values.add(item.asString()));
        return List.copyOf(values);
    }

    private static Measure verifiedMeasure(JsonNode node, Set<Double> stated) {
        if (!node.isObject() || !stated.contains(node.path("value").asDouble())) {
            return null;
        }
        return new Measure(node.path("value").asDouble(), node.path("unit").asString());
    }

    private static Set<Double> numbersIn(String text) {
        Set<Double> values = new LinkedHashSet<>();
        Matcher matcher = NUMBER.matcher(asciiDigits(text).replace(",", ""));
        while (matcher.find()) {
            values.add(Double.parseDouble(matcher.group()));
        }
        return values;
    }

    private static String asciiDigits(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char character : text.toCharArray()) {
            result.append(character >= '०' && character <= '९'
                    ? (char) ('0' + (character - '०'))
                    : character);
        }
        return result.toString();
    }

    private static Map<String, Object> buildSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("language", Map.of("type", "string", "enum", List.of("en", "hi")));
        properties.put("english_question", Map.of("type", "string"));
        properties.put("search_terms", Map.of("type", "string"));
        properties.put("topics", Map.of("type", "array", "items", Map.of("type", "string", "enum", TOPICS)));
        properties.put("plot_area", nullableMeasure(List.of("sqm", "sqft", "sqyd")));
        properties.put("road_width", nullableMeasure(List.of("m", "ft")));
        properties.put("building_height", nullableMeasure(List.of("m", "ft")));
        properties.put("built_up_area", nullableMeasure(List.of("sqm", "sqft", "sqyd")));
        properties.put("building_use", Map.of("type", "string", "enum", List.of(
                "residential", "commercial", "shop_cum_residence", "industrial", "public",
                "agricultural", "other", "unknown")));
        properties.put("rule_refs", Map.of("type", "array", "items", Map.of("type", "string")));
        return Map.of(
                "type", "object",
                "properties", properties,
                "required", List.copyOf(properties.keySet()),
                "additionalProperties", false);
    }

    private static Map<String, Object> nullableMeasure(List<String> units) {
        Map<String, Object> measure = Map.of(
                "type", "object",
                "properties", Map.of(
                        "value", Map.of("type", "number"),
                        "unit", Map.of("type", "string", "enum", units)),
                "required", Arrays.asList("value", "unit"),
                "additionalProperties", false);
        return Map.of("anyOf", List.of(measure, Map.of("type", "null")));
    }

    public record Analysis(
            String language,
            String englishQuestion,
            String searchTerms,
            List<String> topics,
            Measure plotArea,
            Measure roadWidth,
            Measure buildingHeight,
            Measure builtUpArea,
            String buildingUse,
            List<String> ruleRefs,
            String model) {
    }

    public record Measure(Double value, String unit) {
    }
}

package org.springboot.backend.retrieval;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springboot.backend.calc.Units;
import org.springboot.backend.llm.LlmClient;
import org.springboot.backend.llm.LlmException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Turns an English or Hindi question into English search terms plus a structured
 * {@link QueryIntent}. Uses Claude when an API key is set; otherwise (or if Claude fails)
 * uses a keyword-and-regex fallback so search still works offline.
 */
@Component
public class QueryNormalizer {

    private static final Logger log = LoggerFactory.getLogger(QueryNormalizer.class);

    private final LlmClient llm;
    private final JsonMapper json;

    public QueryNormalizer(LlmClient llm, JsonMapper json) {
        this.llm = llm;
        this.json = json;
    }

    public QueryIntent normalize(String question) {
        String q = Normalizer.normalize(question, Normalizer.Form.NFC).strip();
        if (llm.isConfigured()) {
            try {
                return withClaude(q);
            } catch (LlmException | IllegalArgumentException e) {
                // Never log the question itself.
                log.warn("Claude normalisation failed, using offline fallback: {}", e.getMessage());
            }
        }
        return offline(q);
    }

    // ---------------------------------------------------------------- Claude

    private static final String SYSTEM_PROMPT = """
            You prepare search input for a tool that answers questions on the Madhya Pradesh \
            Bhumi Vikas Rules, 2012 (English text). The user is a building-permission clerk at \
            the Bhopal Municipal Corporation. Questions come in English, Hindi (Devanagari) or \
            romanised Hindi.

            Fill the fields as follows:
            - language: "hi" if the question is written mainly in Hindi (Devanagari or romanised), else "en".
            - english_question: a faithful English translation. Do not add facts.
            - search_terms: 5 to 15 English keywords phrased the way Indian building bye-laws \
            phrase them, e.g. "front open space", "floor area ratio", "permission fee", "built up area".
            - topics: every topic from the list below that the question is about (may be empty).
            - plot_area, road_width, building_height, built_up_area: copy ONLY numbers the question \
            states, with the unit exactly as stated. Never convert units, never calculate, never guess; \
            use null when the question does not state the value. "gaj" means sqyd. The width of the \
            road or street in front of the plot is road_width; the building's own height is building_height.
            - building_use: the use the question states, else "unknown".
            - rule_refs: rule numbers the question names explicitly, e.g. "21" or "2(5)(c)"; else empty.

            Topics:
            """ + topicList();

    private static String topicList() {
        StringBuilder sb = new StringBuilder();
        for (Topic t : Topic.values()) {
            sb.append("- ").append(t.name()).append(": ").append(t.description).append('\n');
        }
        return sb.toString();
    }

    private static final Map<String, Object> SCHEMA = buildSchema();

    private static Map<String, Object> buildSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("language", Map.of("type", "string", "enum", List.of("en", "hi")));
        props.put("english_question", Map.of("type", "string"));
        props.put("search_terms", Map.of("type", "string"));
        props.put("topics", Map.of("type", "array", "items",
                Map.of("type", "string", "enum", Arrays.stream(Topic.values()).map(Enum::name).toList())));
        props.put("plot_area", nullableMeasure(List.of("sqm", "sqft", "sqyd")));
        props.put("road_width", nullableMeasure(List.of("m", "ft")));
        props.put("building_height", nullableMeasure(List.of("m", "ft")));
        props.put("built_up_area", nullableMeasure(List.of("sqm", "sqft", "sqyd")));
        props.put("building_use", Map.of("type", "string", "enum",
                List.of("residential", "commercial", "shop_cum_residence", "industrial", "public", "agricultural", "other", "unknown")));
        props.put("rule_refs", Map.of("type", "array", "items", Map.of("type", "string")));
        return Map.of(
                "type", "object",
                "properties", props,
                "required", List.copyOf(props.keySet()),
                "additionalProperties", false);
    }

    private static Map<String, Object> nullableMeasure(List<String> units) {
        Map<String, Object> measure = Map.of(
                "type", "object",
                "properties", Map.of(
                        "value", Map.of("type", "number"),
                        "unit", Map.of("type", "string", "enum", units)),
                "required", List.of("value", "unit"),
                "additionalProperties", false);
        return Map.of("anyOf", List.of(measure, Map.of("type", "null")));
    }

    private QueryIntent withClaude(String question) {
        String reply = llm.completeJson(SYSTEM_PROMPT, "Question:\n" + question, SCHEMA, LlmClient.Effort.low);
        JsonNode n = json.readTree(reply);

        List<Topic> topics = new ArrayList<>();
        for (JsonNode t : n.path("topics")) {
            topics.add(Topic.valueOf(t.asString()));
        }
        List<String> refs = new ArrayList<>();
        n.path("rule_refs").forEach(r -> refs.add(r.asString()));
        String use = n.path("building_use").asString("unknown");

        Set<Double> statedNumbers = numbersIn(question);
        return new QueryIntent(
                n.path("language").asString("en"),
                n.path("english_question").asString(question),
                n.path("search_terms").asString(""),
                topics,
                area(n.path("plot_area"), statedNumbers),
                length(n.path("road_width"), statedNumbers),
                length(n.path("building_height"), statedNumbers),
                area(n.path("built_up_area"), statedNumbers),
                "unknown".equals(use) ? null : use,
                refs,
                "claude");
    }

    /** Keeps a number from Claude only if the question really contains it. */
    private static Double area(JsonNode measure, Set<Double> stated) {
        if (!measure.isObject() || !stated.contains(measure.path("value").asDouble())) {
            return null;
        }
        return Units.areaToSqm(measure.path("value").asDouble(), measure.path("unit").asString());
    }

    private static Double length(JsonNode measure, Set<Double> stated) {
        if (!measure.isObject() || !stated.contains(measure.path("value").asDouble())) {
            return null;
        }
        return Units.lengthToM(measure.path("value").asDouble(), measure.path("unit").asString());
    }

    private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");

    static Set<Double> numbersIn(String text) {
        Set<Double> numbers = new LinkedHashSet<>();
        Matcher m = NUMBER.matcher(asciiDigits(text).replace(",", ""));
        while (m.find()) {
            numbers.add(Double.parseDouble(m.group()));
        }
        return numbers;
    }

    /** Devanagari digits (०-९) to ASCII. */
    static String asciiDigits(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            sb.append(c >= '०' && c <= '९' ? (char) ('0' + (c - '०')) : c);
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- offline fallback

    /** Common Hindi words in counter questions and their English bye-law terms. */
    private static final String[][] HINDI_GLOSSARY = {
            {"प्लॉट", "plot"}, {"भूखंड", "plot"}, {"वर्ग मीटर", "square meters"}, {"सड़क", "road street"},
            {"चौड़ी", "width"}, {"चौड़ाई", "width"}, {"आगे", "front"}, {"सामने", "front"}, {"पीछे", "rear"},
            {"बगल", "side"}, {"खाली जगह", "open space"}, {"जगह खाली", "open space"}, {"खुली जगह", "open space"},
            {"फीस", "fees"}, {"शुल्क", "fees"}, {"बिल्डिंग परमिशन", "building permission"},
            {"भवन अनुज्ञा", "building permission"}, {"अनुमति", "permission"}, {"परमिशन", "permission"},
            {"मंजिल", "floors storey"}, {"ऊंचाई", "height"}, {"ऊँचाई", "height"}, {"पार्किंग", "parking"},
            {"लिफ्ट", "lift"}, {"फार्म हाउस", "farm house"}, {"बालकनी", "balcony"}, {"बिजली", "electric line"},
            {"नक्शा", "plan"}, {"वैधता", "validity valid"}, {"नवीनीकरण", "renewal revalidation"},
            {"दस्तावेज", "documents"}, {"मकान", "residential house"}, {"आवासीय", "residential"},
            {"घर", "residential house"}, {"दुकान", "shop commercial"}, {"नियम", "rule"}, {"विकास योजना", "development plan"},
    };

    private static final String M = "(m|mtrs?|meters?|metres?|मीटर|ft|feet|फीट|फुट)";
    private static final Pattern AREA = nfc(
            "(\\d+(?:\\.\\d+)?)\\s*(sq\\.?\\s*m(?:et(?:er|re)s?|trs?)?\\b|sqm|square\\s*met(?:er|re)s?|m2|m²|वर्ग\\s*मीटर"
                    + "|sq\\.?\\s*f(?:ee)?t|sqft|square\\s*f(?:ee|oo)t|वर्ग\\s*फ(?:ु|ी)ट|sq\\.?\\s*yd|square\\s*yards?|गज|gaj)");
    private static final Pattern ROAD = nfc(
            "(\\d+(?:\\.\\d+)?)\\s*" + M + "\\s*(?:wide|चौड़ी|चौड़ा)?\\s*(?:road|street|सड़क|रोड|गली)");
    private static final Pattern HEIGHT_AFTER = nfc(
            "(\\d+(?:\\.\\d+)?)\\s*" + M + "\\s*(?:tall|high|ऊंच|ऊँच)");
    private static final Pattern HEIGHT_BEFORE = nfc(
            "(?:height|taller than|higher than|ऊंचाई|ऊँचाई)\\D{0,15}?(\\d+(?:\\.\\d+)?)\\s*" + M);
    private static final Pattern RULE_REF = nfc(
            "(?:rule|नियम)\\s*(\\d+(?:\\s*-?\\s*[A-Za-z]\\b)?(?:\\s*\\(\\w+\\))*)");
    private static final Pattern RESIDENTIAL = nfc("residential|(?<!farm )\\bhouse\\b|\\bhome\\b|मकान|आवासीय|घर");
    private static final Pattern COMMERCIAL = nfc("commercial|\\bshops?\\b|दुकान|व्यावसायिक");

    private static Pattern nfc(String regex) {
        return Pattern.compile(Normalizer.normalize(regex, Normalizer.Form.NFC),
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    static QueryIntent offline(String question) {
        String q = asciiDigits(Normalizer.normalize(question, Normalizer.Form.NFC));
        boolean hindi = q.codePoints().anyMatch(c -> c >= 0x0900 && c <= 0x097F);

        String english = q;
        if (hindi) {
            StringBuilder words = new StringBuilder();
            for (String[] entry : HINDI_GLOSSARY) {
                if (q.contains(Normalizer.normalize(entry[0], Normalizer.Form.NFC))) {
                    words.append(entry[1]).append(' ');
                }
            }
            english = words.toString().strip();
        }

        List<Topic> topics = new ArrayList<>();
        for (Topic t : Topic.values()) {
            if (t.cue.matcher(q).find() || t.cue.matcher(english).find()) {
                topics.add(t);
            }
        }

        StringBuilder terms = new StringBuilder(english);
        topics.forEach(t -> terms.append(' ').append(t.searchWords));

        Double plot = null;
        Double builtUp = null;
        Matcher area = AREA.matcher(q);
        if (area.find()) {
            double sqm = Units.areaToSqm(Double.parseDouble(area.group(1)), areaUnit(area.group(2)));
            if (q.toLowerCase(Locale.ROOT).contains("built")) {
                builtUp = sqm;
            } else {
                plot = sqm;
            }
        }

        List<String> refs = new ArrayList<>();
        Matcher ref = RULE_REF.matcher(q);
        while (ref.find()) {
            refs.add(ref.group(1).replace(" ", ""));
        }

        String use = RESIDENTIAL.matcher(q).find() ? "residential" : COMMERCIAL.matcher(q).find() ? "commercial" : null;

        return new QueryIntent(
                hindi ? "hi" : "en",
                hindi ? english : q,
                terms.toString(),
                topics,
                plot,
                firstLength(q, ROAD),
                firstLength(q, HEIGHT_AFTER) != null ? firstLength(q, HEIGHT_AFTER) : firstLength(q, HEIGHT_BEFORE),
                builtUp,
                use,
                refs,
                "offline");
    }

    private static Double firstLength(String text, Pattern pattern) {
        Matcher m = pattern.matcher(text);
        if (!m.find()) {
            return null;
        }
        String unit = m.group(2).toLowerCase(Locale.ROOT);
        boolean feet = unit.startsWith("f") || unit.equals("फीट") || unit.equals("फुट");
        return Units.lengthToM(Double.parseDouble(m.group(1)), feet ? "ft" : "m");
    }

    private static String areaUnit(String unitText) {
        String u = unitText.toLowerCase(Locale.ROOT);
        if (u.contains("f") || u.contains("फ")) {
            return "sqft";
        }
        if (u.contains("yd") || u.contains("yard") || u.contains("गज") || u.contains("gaj")) {
            return "sqyd";
        }
        return "sqm";
    }
}

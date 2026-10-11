package org.springboot.backend.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springboot.backend.service.AiService;
import org.springboot.backend.util.LanguageDetector;
import org.springboot.backend.util.QueryLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Historical class name retained from the requested backend structure. This is
 * only an HTTP adapter to the separate Python AI service; it has no provider key,
 * prompt, SDK, or direct Claude API access.
 */
@Service
public class ClaudeAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAiService.class);
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");
    private static final Pattern ROAD = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*(m|meters?|metres?|मीटर|ft|feet|फीट|फुट)\\s*(?:wide|चौड़ी|चौड़ा)?\\s*(?:road|street|सड़क|रोड|गली)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern HEIGHT = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*(m|meters?|metres?|मीटर|ft|feet|फीट|फुट)\\s*(?:tall|high|ऊंच|ऊँच)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern FRONTAGE = Pattern.compile(
            "(?:frontage|plot front|मुखभाग)\\s*(?:is|of|का)?\\s*(\\d+(?:\\.\\d+)?)\\s*(m|meters?|metres?|मीटर|ft|feet|फीट|फुट)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern AREA = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*(sqm|sq\\.?\\s*m|square\\s*meters?|वर्ग\\s*मीटर|sqft|sq\\.?\\s*ft|gaj|गज)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final HttpClient http;
    private final JsonMapper json;
    private final LanguageDetector languages;
    private final URI endpoint;
    private final String token;
    private final boolean enabled;
    private final Duration timeout;

    public ClaudeAiService(
            HttpClient http,
            JsonMapper json,
            LanguageDetector languages,
            @Value("${bvs.ai-service.base-url}") String baseUrl,
            @Value("${bvs.ai-service.internal-token:}") String token,
            @Value("${bvs.ai-service.enabled:true}") boolean enabled,
            @Value("${bvs.ai-service.timeout:5s}") Duration timeout) {
        this.http = http;
        this.json = json;
        this.languages = languages;
        this.endpoint = URI.create(baseUrl.replaceFirst("/+$", "") + "/internal/v1/query-intents");
        this.token = token == null ? "" : token.strip();
        this.enabled = enabled;
        this.timeout = timeout;
    }

    @Override
    public Analysis analyse(String question) {
        String normalized = Normalizer.normalize(question, Normalizer.Form.NFC).strip();
        if (!enabled) {
            return offline(normalized);
        }
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(endpoint)
                    .timeout(timeout)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            json.writeValueAsString(Map.of("question", normalized))));
            if (!token.isBlank()) {
                request.header("x-internal-token", token);
            }
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("AI service returned HTTP " + response.statusCode());
            }
            return fromResponse(normalized, json.readTree(response.body()));
        } catch (IOException | RuntimeException e) {
            log.warn("AI service unavailable or invalid; using deterministic parser: {}", e.getMessage());
            return offline(normalized);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return offline(normalized);
        }
    }

    private Analysis fromResponse(String question, JsonNode node) {
        List<String> topics = strings(node.path("topics"));
        List<String> refs = strings(node.path("ruleRefs"));
        refs = java.util.stream.Stream.concat(refs.stream(),QueryLexicon.ruleHints(question).stream()).distinct().toList();
        return new Analysis(
                "hi".equals(node.path("language").asString()) ? "hi" : "en",
                node.path("englishQuestion").asString(question),
                node.path("searchTerms").asString(question),
                topics,
                verifiedArea(question, node.path("plotArea")),
                verifiedLength(question, node.path("roadWidth")),
                verifiedLength(question, node.path("plotFrontage")),
                verifiedLength(question, node.path("buildingHeight")),
                verifiedArea(question, node.path("builtUpArea")),
                nullable(node.path("buildingUse").asString("unknown")),
                electricCategory(node.path("electricLineCategory").asString("unknown")),
                buildingForm(node.path("buildingForm").asString("unknown")),
                refs,
                "ai-service",
                verifiedStoreys(question,node.path("storeys").asInt(0)));
    }

    private Analysis offline(String question) {
        String lower = question.toLowerCase(Locale.ROOT);
        List<String> topics = new ArrayList<>();
        if (lower.matches("(?s).*(setback|open space|खाली जगह|सामने).*")) topics.add("SETBACK");
        if (lower.matches("(?s).*(fee|fees|फीस|शुल्क).*")) topics.add("FEES");
        if (question.matches("(?s).*\\bFAR\\b.*") || lower.matches("(?s).*(floor area|एफएआर).*")) topics.add("FAR");
        if (lower.matches("(?s).*(electric|voltage|बिजली).*")) topics.add("ELECTRIC_LINES");
        if (lower.matches("(?s).*(valid|renew|वैधता|नवीनीकरण).*")) topics.add("VALIDITY");
        if (!lower.matches("(?s).*(lift|escalator|लिफ्ट).*") && QueryLexicon.expand(lower).matches("(?s).*(height|high.?rise|taller).*")) topics.add("HEIGHT");
        if (lower.matches("(?s).*(farm.?house|फार्म).*")) topics.add("FARM_HOUSE");
        if (QueryLexicon.expand(lower).matches("(?s).*(front open space|rear open space|side open space).*")) topics.add("SETBACK");
        if (QueryLexicon.expand(lower).matches("(?s).*(permission fee).*")) topics.add("FEES");
        List<String> refs = new ArrayList<>();
        Matcher explicit = Pattern.compile("(?:rule|नियम)\\s*([0-9]+(?:\\([^)]*\\))?)", Pattern.CASE_INSENSITIVE)
                .matcher(asciiDigits(question));
        while (explicit.find()) refs.add(explicit.group(1));
        refs.addAll(QueryLexicon.ruleHints(question));
        return new Analysis(
                languages.detect(question), question, QueryLexicon.expand(question), topics.stream().distinct().toList(),
                firstArea(question), firstLength(question, ROAD), firstLength(question, FRONTAGE),
                firstLength(question, HEIGHT), null,
                lower.matches("(?s).*(residential|house|home|आवासीय|मकान|घर).*") ? "residential" : null,
                offlineElectricCategory(lower),
                offlineBuildingForm(lower),
                refs.stream().distinct().toList(), "offline", offlineStoreys(question));
    }

    private static List<String> strings(JsonNode node) {
        List<String> result = new ArrayList<>();
        node.forEach(value -> result.add(value.asString()));
        return List.copyOf(result);
    }

    private static Double verifiedArea(String question, JsonNode measure) {
        return measure.isObject() && containsNumber(question, measure.path("value").asDouble())
                ? areaToSquareMeters(measure.path("value").asDouble(), measure.path("unit").asString())
                : null;
    }

    private static Double verifiedLength(String question, JsonNode measure) {
        return measure.isObject() && containsNumber(question, measure.path("value").asDouble())
                ? lengthToMeters(measure.path("value").asDouble(), measure.path("unit").asString())
                : null;
    }

    private static boolean containsNumber(String question, double expected) {
        Matcher matcher = NUMBER.matcher(asciiDigits(question).replace(",", ""));
        while (matcher.find()) {
            if (Double.compare(Double.parseDouble(matcher.group()), expected) == 0) return true;
        }
        return false;
    }

    private static Double firstArea(String question) {
        Matcher matcher = AREA.matcher(asciiDigits(question));
        return matcher.find() ? areaToSquareMeters(Double.parseDouble(matcher.group(1)), matcher.group(2)) : null;
    }

    private static Double firstLength(String question, Pattern pattern) {
        Matcher matcher = pattern.matcher(asciiDigits(question));
        return matcher.find() ? lengthToMeters(Double.parseDouble(matcher.group(1)), matcher.group(2)) : null;
    }

    private static double areaToSquareMeters(double value, String unit) {
        String normalized = unit.toLowerCase(Locale.ROOT).replace(" ", "");
        if (normalized.contains("ft")) return value / 10.763910416709722;
        if (normalized.contains("gaj") || normalized.contains("गज")) return value * 0.83612736;
        return value;
    }

    private static double lengthToMeters(double value, String unit) {
        String normalized = unit.toLowerCase(Locale.ROOT);
        return normalized.startsWith("f") || normalized.equals("फीट") || normalized.equals("फुट")
                ? value * 0.3048 : value;
    }

    private static String asciiDigits(String text) {
        StringBuilder value = new StringBuilder(text.length());
        for (char character : text.toCharArray()) {
            value.append(character >= '०' && character <= '९'
                    ? (char) ('0' + character - '०') : character);
        }
        return value.toString();
    }

    private static String nullable(String value) {
        return value == null || value.isBlank() || "unknown".equals(value) ? null : value;
    }

    private static String electricCategory(String value) {
        return switch (value) {
            case "low_medium", "high_up_to_33000", "extra_high_above_33000" -> value;
            default -> "unknown";
        };
    }

    private static String offlineElectricCategory(String question) {
        if (question.matches("(?s).*(extra high|अति उच्च).*(33000|33).*")) return "extra_high_above_33000";
        if (question.matches("(?s).*(high voltage|उच्च वोल्टेज|33000|33\\s*kv).*")) return "high_up_to_33000";
        if (question.matches("(?s).*(low.?voltage|medium.?voltage|service line|लो वोल्टेज).*") ) return "low_medium";
        return "unknown";
    }

    private static String buildingForm(String value) {
        return switch (value) {
            case "detached", "semi_detached", "row" -> value;
            default -> "unknown";
        };
    }

    private static String offlineBuildingForm(String question) {
        if (question.matches("(?s).*(semi[- ]?detached|अर्ध.?स्वतंत्र).*") ) return "semi_detached";
        if (question.matches("(?s).*(row house|row building|पंक्ति मकान).*") ) return "row";
        if (question.matches("(?s).*(detached|स्वतंत्र मकान).*") ) return "detached";
        return "unknown";
    }

    private static Integer verifiedStoreys(String question,int proposed) {
        return proposed>0 && containsNumber(question,proposed) ? proposed:null;
    }
    private static Integer offlineStoreys(String question) {
        Matcher m=Pattern.compile("(\\d+)\\s*(?:storeys?|stories|floors?|मंजिल)",Pattern.CASE_INSENSITIVE).matcher(asciiDigits(question));
        return m.find()?verifiedStoreys(question,Integer.parseInt(m.group(1))):null;
    }
}

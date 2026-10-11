package org.springboot.backend.retrieval;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springboot.backend.calc.Units;
import org.springboot.backend.integration.ai.AiIntentClient;
import org.springboot.backend.integration.ai.AiServiceException;
import org.springframework.stereotype.Component;

/**
 * Converts a question into deterministic backend search input. The separate AI
 * service may propose an intent; this class verifies its numbers and retains a
 * local parser as a fail-safe.
 */
@Component
public class QueryNormalizer {

    private static final Logger log = LoggerFactory.getLogger(QueryNormalizer.class);

    private final AiIntentClient aiService;

    public QueryNormalizer(AiIntentClient aiService) {
        this.aiService = aiService;
    }

    public QueryIntent normalize(String question) {
        String normalized = Normalizer.normalize(question, Normalizer.Form.NFC).strip();
        if (aiService.isEnabled()) {
            try {
                return fromAiService(normalized, aiService.analyse(normalized));
            } catch (AiServiceException | IllegalArgumentException e) {
                // Questions may contain sensitive property context, so never log them.
                log.warn("AI intent service unavailable or invalid; using offline parser: {}", e.getMessage());
            }
        }
        return offline(normalized);
    }

    private static QueryIntent fromAiService(String question, AiIntentClient.Analysis analysis) {
        List<Topic> topics = new ArrayList<>();
        for (String value : analysis.topics()) {
            try {
                topics.add(Topic.valueOf(value));
            } catch (IllegalArgumentException ignored) {
                // A newer AI-service topic is ignored until this backend supports it.
            }
        }
        Set<Double> statedNumbers = numbersIn(question);
        String use = analysis.buildingUse();
        return new QueryIntent(
                "hi".equals(analysis.language()) ? "hi" : "en",
                blankTo(analysis.englishQuestion(), question),
                blankTo(analysis.searchTerms(), question),
                List.copyOf(topics),
                area(analysis.plotArea(), statedNumbers),
                length(analysis.roadWidth(), statedNumbers),
                length(analysis.buildingHeight(), statedNumbers),
                area(analysis.builtUpArea(), statedNumbers),
                use == null || use.isBlank() || "unknown".equals(use) ? null : use,
                analysis.ruleRefs() == null ? List.of() : List.copyOf(analysis.ruleRefs()),
                "ai-service");
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /** Accept a proposed number only if that exact number occurs in the question. */
    private static Double area(AiIntentClient.Measure measure, Set<Double> stated) {
        if (measure == null || measure.value() == null || !stated.contains(measure.value())) {
            return null;
        }
        return Units.areaToSqm(measure.value(), measure.unit());
    }

    private static Double length(AiIntentClient.Measure measure, Set<Double> stated) {
        if (measure == null || measure.value() == null || !stated.contains(measure.value())) {
            return null;
        }
        return Units.lengthToM(measure.value(), measure.unit());
    }

    private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");

    static Set<Double> numbersIn(String text) {
        Set<Double> numbers = new LinkedHashSet<>();
        Matcher matcher = NUMBER.matcher(asciiDigits(text).replace(",", ""));
        while (matcher.find()) {
            numbers.add(Double.parseDouble(matcher.group()));
        }
        return numbers;
    }

    static String asciiDigits(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char character : text.toCharArray()) {
            result.append(character >= '०' && character <= '९'
                    ? (char) ('0' + (character - '०'))
                    : character);
        }
        return result.toString();
    }

    /** Common Hindi terms used only by the backend's deterministic fallback. */
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
        for (Topic topic : Topic.values()) {
            if (topic.cue.matcher(q).find() || topic.cue.matcher(english).find()) {
                topics.add(topic);
            }
        }

        StringBuilder terms = new StringBuilder(english);
        topics.forEach(topic -> terms.append(' ').append(topic.searchWords));

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

        String use = RESIDENTIAL.matcher(q).find()
                ? "residential"
                : COMMERCIAL.matcher(q).find() ? "commercial" : null;

        return new QueryIntent(
                hindi ? "hi" : "en",
                hindi ? english : q,
                terms.toString(),
                topics,
                plot,
                firstLength(q, ROAD),
                firstLength(q, HEIGHT_AFTER) != null
                        ? firstLength(q, HEIGHT_AFTER)
                        : firstLength(q, HEIGHT_BEFORE),
                builtUp,
                use,
                refs,
                "offline");
    }

    private static Double firstLength(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String unit = matcher.group(2).toLowerCase(Locale.ROOT);
        boolean feet = unit.startsWith("f") || unit.equals("फीट") || unit.equals("फुट");
        return Units.lengthToM(Double.parseDouble(matcher.group(1)), feet ? "ft" : "m");
    }

    private static String areaUnit(String unitText) {
        String unit = unitText.toLowerCase(Locale.ROOT);
        if (unit.contains("f") || unit.contains("फ")) {
            return "sqft";
        }
        if (unit.contains("yd") || unit.contains("yard") || unit.contains("गज") || unit.contains("gaj")) {
            return "sqyd";
        }
        return "sqm";
    }
}

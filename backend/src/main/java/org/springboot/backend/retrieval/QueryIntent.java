package org.springboot.backend.retrieval;

import java.util.List;

/**
 * What the clerk's question is about, normalised to English. Numbers are always in
 * metres / square metres; unit conversion happens in Java, not in the LLM.
 *
 * @param language        "en" or "hi" - the language the answer must be in
 * @param englishQuestion the question in plain English
 * @param searchTerms     English keywords for the rule search
 * @param plotAreaSqm     null when the question does not give it
 * @param buildingUse     e.g. "residential", "commercial"; null if not stated
 * @param ruleRefs        rule numbers the question names explicitly, e.g. "21", "2(5)(c)"
 * @param source          "claude" or "offline" - who produced this intent
 */
public record QueryIntent(
        String language,
        String englishQuestion,
        String searchTerms,
        List<Topic> topics,
        Double plotAreaSqm,
        Double roadWidthM,
        Double buildingHeightM,
        Double builtUpAreaSqm,
        String buildingUse,
        List<String> ruleRefs,
        String source) {
}

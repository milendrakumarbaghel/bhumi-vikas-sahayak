package org.springboot.backend.citation;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springboot.backend.integration.ai.AiAnswerClient;
import org.springboot.backend.integration.ai.AiAnswerClient.CitedQuote;
import org.springboot.backend.rules.Chunk;
import org.springboot.backend.rules.RuleRepository;

class AnswerVerifierTest {

    static RuleRepository rules;
    static AnswerVerifier verifier;
    static Map<String, Chunk> chunks;
    static String front;   // Rule 56 (1): front open space table
    static String fees;    // Rule 21 (3)(b): residential fee table

    @BeforeAll
    static void setUp() {
        rules = new RuleRepository(Path.of("../track-1-bhumi-vikas-sahayak/data/bhumi_vikas_rules.jsonl"));
        verifier = new AnswerVerifier(rules);
        chunks = rules.chunks().stream().collect(Collectors.toMap(Chunk::id, Function.identity()));
        front = idOf("Rule 56 (1)");
        fees = idOf("Rule 21 (3)(b)");
    }

    static String idOf(String label) {
        return rules.chunks().stream().filter(c -> c.label().equals(label)).findFirst().orElseThrow().id();
    }

    static AiAnswerClient.Proposal answered(String answer, CitedQuote... quotes) {
        return new AiAnswerClient.Proposal("answered", "en", answer, List.of(quotes), List.of(), "test");
    }

    AnswerVerifier.Result check(AiAnswerClient.Proposal p) {
        return verifier.verify(p, chunks, "My house faces a 10 m wide street and will be 9 m tall.");
    }

    @Test
    void acceptsAnExactQuoteEvenWithDifferentTableSpacing() {
        AnswerVerifier.Result r = check(answered("Under Rule 56(1) the front open space is 3.6 meters.",
                new CitedQuote(front, "2. More than 9.0 meters and up to 12 meters 3.6 meters")));
        assertThat(r.passed()).as(r.problems().toString()).isTrue();
        assertThat(r.citations()).singleElement().satisfies(c -> assertThat(c.chunk().rule()).isEqualTo("56"));
    }

    @Test
    void toleratesCurlyQuotesAndATrailingFullStop() {
        AnswerVerifier.Result r = check(answered("Rule 56 sets 3.6 m.",
                new CitedQuote(front, "“More than 9.0 meters and up to 12 meters 3.6 meters.”")));
        assertThat(r.passed()).as(r.problems().toString()).isTrue();
    }

    @Test
    void rejectsAQuoteThatIsNotInTheRule() {
        AnswerVerifier.Result r = check(answered("Rule 56 sets 3.6 m.",
                new CitedQuote(front, "More than 9.0 meters and up to 12 meters 4.0 meters")));
        assertThat(r.passed()).isFalse();
        assertThat(r.problems()).anyMatch(p -> p.contains("not in the text of Rule 56"));
    }

    @Test
    void rejectsAQuoteFromTheWrongRule() {
        // A real sentence, but from Rule 56, cited against the Rule 21 extract.
        AnswerVerifier.Result r = check(answered("Rule 21 sets 3.6 m.",
                new CitedQuote(fees, "More than 9.0 meters and up to 12 meters 3.6 meters")));
        assertThat(r.passed()).isFalse();
    }

    @Test
    void rejectsInventedNumbers() {
        AnswerVerifier.Result r = check(answered("Rule 56(1) requires 3.6 meters and a fee of Rs. 7,777.",
                new CitedQuote(front, "More than 9.0 meters and up to 12 meters 3.6 meters")));
        assertThat(r.passed()).isFalse();
        assertThat(r.problems()).anyMatch(p -> p.contains("7777"));
    }

    @Test
    void acceptsIndianNumberFormatsFromTheRule() {
        AnswerVerifier.Result r = check(answered("Under Rule 21(3)(b) a house of 76-125 sq m pays Rs. 1,250.",
                new CitedQuote(fees, "76         125       1,250")));
        assertThat(r.passed()).as(r.problems().toString()).isTrue();
    }

    @Test
    void rejectsRuleNumbersThatDoNotExist() {
        AnswerVerifier.Result r = check(answered("Rule 56(1) and Rule 999 require 3.6 meters.",
                new CitedQuote(front, "More than 9.0 meters and up to 12 meters 3.6 meters")));
        assertThat(r.passed()).isFalse();
        assertThat(r.problems()).anyMatch(p -> p.contains("Rule 999"));
    }

    @Test
    void answeredNeedsAtLeastOneVerifiedCitation() {
        assertThat(check(answered("Rule 56 requires 3.6 m.")).passed()).isFalse();
        assertThat(check(answered("Rule 56 requires 3.6 m.", new CitedQuote(front, "3.6 m"))).passed())
                .as("too short to be meaningful").isFalse();
        assertThat(check(answered("Rule 56 requires 3.6 m.", new CitedQuote("999#0", "anything at all here")))
                .passed()).isFalse();
    }

    @Test
    void hindiAnswerWithRuleNumbersAndDigitsPasses() {
        AnswerVerifier.Result r = check(new AiAnswerClient.Proposal("answered", "hi",
                "नियम 56(1) के अनुसार 9 मीटर तक की सड़क पर आगे 3.0 मीटर खाली जगह छोड़नी होगी।",
                List.of(new CitedQuote(front, "1.    Up to 9.0 meters                                            3.0 meters")),
                List.of(), "test"));
        assertThat(r.passed()).as(r.problems().toString()).isTrue();
    }

    @Test
    void notFoundAndNeedsMoreInfoRules() {
        assertThat(check(new AiAnswerClient.Proposal("not_found", "en", "The rules do not cover property tax.",
                List.of(), List.of(), "t")).passed()).isTrue();
        assertThat(check(new AiAnswerClient.Proposal("needs_more_info", "en", "What is the road width?",
                List.of(), List.of("road width"), "t")).passed()).isTrue();
        assertThat(check(new AiAnswerClient.Proposal("needs_more_info", "en", "Tell me more.",
                List.of(), List.of(), "t")).passed()).isFalse();
    }

    @Test
    void normalisesNumbers() {
        assertThat(AnswerVerifier.numbersIn("Rs. 1,25,000 and 3.60 m, 0.40 hectare; items 1, 2"))
                .contains("125000", "3.6", "0.4", "1", "2");
    }
}

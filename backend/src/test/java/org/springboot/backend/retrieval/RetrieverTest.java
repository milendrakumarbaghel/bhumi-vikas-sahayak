package org.springboot.backend.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springboot.backend.llm.LlmClient;
import org.springboot.backend.rules.RuleRepository;
import tools.jackson.databind.json.JsonMapper;

/** Retrieval with the offline normaliser (no API key): the right rule must be in the top chunks. */
class RetrieverTest {

    static Retriever retriever;

    @BeforeAll
    static void setUp() {
        RuleRepository rules = new RuleRepository(Path.of("../track-1-bhumi-vikas-sahayak/data/bhumi_vikas_rules.jsonl"));
        QueryNormalizer normalizer = new QueryNormalizer(new NoLlm(), JsonMapper.builder().build());
        retriever = new Retriever(normalizer, new RuleSearch(rules), rules);
    }

    static Stream<Arguments> sampleQuestions() {
        return Stream.of(
                Arguments.of("My house faces a 10 m wide street and will be 9 m tall. How much open space do I leave at the front?", List.of("56")),
                Arguments.of("I want a farm house on agricultural land. What is the minimum plot size and the maximum FAR?", List.of("17")),
                Arguments.of("My development permission was granted in 2023. How long does it stay valid, and how many times can I renew it?", List.of("23")),
                Arguments.of("I applied for building permission and the authority has not replied. What happens if they miss the deadline?", List.of("28")),
                Arguments.of("How far must my balcony stay from a low-voltage overhead electric line, vertically and horizontally?", List.of("52")),
                Arguments.of("Who is allowed to sign and submit my building plans?", List.of("18")),
                Arguments.of("What documents go with an application for building permission?", List.of("16", "14")),
                Arguments.of("What is the fee for building permission for a residential house?", List.of("21")),
                Arguments.of("Can I change my plan after it is sanctioned?", List.of("24", "19")),
                Arguments.of("What is the minimum rear open space for a 150 sq m residential plot?", List.of("56")),
                Arguments.of("Do I need separate development permission for a 300 sq m plot that matches the Development Plan? (Tip: this changed in August 2026.)", List.of("2", "12")),
                Arguments.of("Which rule decides what happens when the Development Plan and these rules disagree?", List.of("103")),
                Arguments.of("What are the rules for lifts in a building taller than 15 m?", List.of("83")),
                Arguments.of("मेरा प्लॉट 200 वर्ग मीटर का है और सामने 9 मीटर चौड़ी सड़क है। आगे कितनी जगह खाली छोड़नी होगी?", List.of("56")),
                Arguments.of("बिल्डिंग परमिशन की फीस कितनी है और किस नियम में लिखी है?", List.of("21")));
    }

    @ParameterizedTest
    @MethodSource("sampleQuestions")
    void expectedRuleIsRetrieved(String question, List<String> expectedAnyOf) {
        Retriever.Result result = retriever.retrieve(question);
        List<String> rules = result.chunks().stream().map(c -> c.chunk().rule()).toList();
        assertThat(rules).as(question).containsAnyElementsOf(expectedAnyOf);
        assertThat(result.chunks()).hasSizeLessThanOrEqualTo(Retriever.MAX_CHUNKS);
    }

    @ParameterizedTest
    @MethodSource("sampleQuestions")
    void hindiQuestionsAreDetected(String question, List<String> ignored) {
        boolean devanagari = question.codePoints().anyMatch(c -> c >= 0x0900 && c <= 0x097F);
        assertThat(retriever.retrieve(question).intent().language()).isEqualTo(devanagari ? "hi" : "en");
    }

    @org.junit.jupiter.api.Test
    void extractsNumbersOffline() {
        QueryIntent hi = QueryNormalizer.offline("मेरा प्लॉट 200 वर्ग मीटर का है और सामने 9 मीटर चौड़ी सड़क है।");
        assertThat(hi.plotAreaSqm()).isEqualTo(200.0);
        assertThat(hi.roadWidthM()).isEqualTo(9.0);

        QueryIntent en = QueryNormalizer.offline("My house faces a 10 m wide street and will be 9 m tall.");
        assertThat(en.roadWidthM()).isEqualTo(10.0);
        assertThat(en.buildingHeightM()).isEqualTo(9.0);

        QueryIntent feet = QueryNormalizer.offline("Plot of 1200 sq ft on a 30 ft road");
        assertThat(feet.plotAreaSqm()).isCloseTo(111.48, org.assertj.core.data.Offset.offset(0.01));
        assertThat(feet.roadWidthM()).isCloseTo(9.144, org.assertj.core.data.Offset.offset(0.001));
    }

    @org.junit.jupiter.api.Test
    void topicCuesMatchWholeWordsOnly() {
        QueryIntent fee = QueryNormalizer.offline("What is the fee for building permission for a residential house?");
        assertThat(fee.topics()).containsExactly(Topic.FEES); // "miss" inside "permission" is not DEEMED_SANCTION
        assertThat(QueryNormalizer.offline("I want a farm house on agricultural land.").buildingUse()).isNull();
    }

    @org.junit.jupiter.api.Test
    void outOfScopeQuestionsPinNoRules() {
        assertThat(QueryNormalizer.offline("How do I pay my property tax online?").topics()).isEmpty();
        assertThat(QueryNormalizer.offline("What is the current market rate for land in Kolar Road, Bhopal?").topics()).isEmpty();
    }

    /** Stand-in for "no API key configured". */
    static class NoLlm implements LlmClient {
        public boolean isConfigured() { return false; }
        public String modelName() { return "none"; }
        public String completeJson(String s, String u, Map<String, Object> schema, Effort e) {
            throw new UnsupportedOperationException();
        }
    }
}

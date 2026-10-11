package org.springboot.backend.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springboot.backend.citation.AnswerVerifier;
import org.springboot.backend.integration.ai.AiAnswerClient;
import org.springboot.backend.integration.ai.AiAnswerClient.CitedQuote;
import org.springboot.backend.integration.ai.AiAnswerClient.Proposal;
import org.springboot.backend.integration.ai.AiIntentClient;
import org.springboot.backend.retrieval.QueryNormalizer;
import org.springboot.backend.retrieval.Retriever;
import org.springboot.backend.retrieval.RuleSearch;
import org.springboot.backend.rules.RuleRepository;

/** Orchestration with a fake AI service: verified quotes only, metadata from the backend. */
class AnswerServiceTest {

    static final String FRONT_Q =
            "My house faces a 10 m wide street and will be 9 m tall. How much open space do I leave at the front?";
    static final String GOOD_QUOTE = "More than 9.0 meters and up to 12 meters 3.6 meters";

    static Retriever retriever;
    static AnswerVerifier verifier;

    @BeforeAll
    static void setUp() {
        RuleRepository rules = new RuleRepository(Path.of("../track-1-bhumi-vikas-sahayak/data/bhumi_vikas_rules.jsonl"));
        QueryNormalizer offline = new QueryNormalizer(new AiIntentClient() {
            public boolean isEnabled() { return false; }
            public Analysis analyse(String question) { throw new UnsupportedOperationException(); }
        });
        retriever = new Retriever(offline, new RuleSearch(rules), rules);
        verifier = new AnswerVerifier(rules);
    }

    static AnswerService withAi(Function<AiAnswerClient.Request, Proposal> reply) {
        return new AnswerService(retriever, reply::apply, verifier);
    }

    static String chunkIdFor(AiAnswerClient.Request request, String label) {
        return request.chunks().stream().filter(c -> c.label().equals(label)).findFirst().orElseThrow().id();
    }

    static Proposal frontAnswer(AiAnswerClient.Request req, String quote) {
        return new Proposal("answered", req.language(), "Rule 56(1) requires a front open space of 3.6 meters.",
                List.of(new CitedQuote(chunkIdFor(req, "Rule 56 (1)"), quote)), List.of(), "fake");
    }

    @Test
    void verifiedAnswerGetsCitationMetadataFromTheBackend() {
        AnswerService.AskResult result = withAi(req -> frontAnswer(req, GOOD_QUOTE)).ask(FRONT_Q, "auto");

        assertThat(result.status()).isEqualTo("answered");
        assertThat(result.verification().passed()).isTrue();
        assertThat(result.verification().attempts()).isEqualTo(1);
        AnswerService.Citation c = result.citations().get(0);
        assertThat(c.rule()).isEqualTo("56");
        assertThat(c.subRule()).isEqualTo("(1)");
        assertThat(c.title()).isEqualTo("Residential Buildings");
        assertThat(c.pdfPages()).isEqualTo("77");
        assertThat(c.quote()).isEqualTo(GOOD_QUOTE);
        assertThat(result.rulesVersion()).contains("6 March 2025");
    }

    @Test
    void retriesOnceWhenTheFirstAnswerFailsVerification() {
        AtomicInteger calls = new AtomicInteger();
        AnswerService.AskResult result = withAi(req -> calls.incrementAndGet() == 1
                ? frontAnswer(req, "More than 9.0 meters and up to 12 meters 5.0 meters") // wrong quote
                : frontAnswer(req, GOOD_QUOTE)).ask(FRONT_Q, "auto");

        assertThat(calls.get()).isEqualTo(2);
        assertThat(result.status()).isEqualTo("answered");
        assertThat(result.verification().attempts()).isEqualTo(2);
    }

    @Test
    void failsClosedToNotFoundAndNeverShowsAnUnverifiedQuote() {
        AtomicInteger calls = new AtomicInteger();
        AnswerService.AskResult result = withAi(req -> {
            calls.incrementAndGet();
            return frontAnswer(req, "The front open space shall be 2 meters");  // invented both times
        }).ask(FRONT_Q, "auto");

        assertThat(calls.get()).isEqualTo(2);
        assertThat(result.status()).isEqualTo("not_found");
        assertThat(result.citations()).isEmpty();
        assertThat(result.answer()).contains("could be verified").doesNotContain("3.6");
        assertThat(result.verification().passed()).isFalse();
        assertThat(result.verification().problems()).isNotEmpty();
    }

    @Test
    void citationToAnExtractThatWasNotSentFailsVerification() {
        AnswerService.AskResult result = withAi(req -> new Proposal("answered", "en", "Rule 56(1) requires 3.6 meters.",
                List.of(new CitedQuote("999#1", GOOD_QUOTE)), List.of(), "fake")).ask(FRONT_Q, "auto");
        assertThat(result.status()).isEqualTo("not_found");
    }

    @Test
    void notFoundHasNoCitations() {
        AnswerService.AskResult result = withAi(req -> new Proposal("not_found", "en", "", List.of(), List.of(), "fake"))
                .ask("How do I pay my property tax online?", "auto");

        assertThat(result.status()).isEqualTo("not_found");
        assertThat(result.citations()).isEmpty();
        assertThat(result.answer()).contains("Not found in the loaded rules");
        assertThat(result.verification().passed()).isTrue();
    }

    @Test
    void hindiQuestionIsAnsweredInHindiAndSmallPlotGetsAmendmentWarning() {
        String[] languageSent = new String[1];
        AnswerService.AskResult result = withAi(req -> {
            languageSent[0] = req.language();
            return new Proposal("answered", req.language(), "नियम 56(1) के अनुसार आगे 3.0 मीटर खाली जगह छोड़नी होगी।",
                    List.of(new CitedQuote(chunkIdFor(req, "Rule 56 (1)"), "Up to 9.0 meters 3.0 meters")), List.of(), "fake");
        }).ask("मेरा प्लॉट 200 वर्ग मीटर का है और सामने 9 मीटर चौड़ी सड़क है। आगे कितनी जगह खाली छोड़नी होगी?", "auto");

        assertThat(languageSent[0]).isEqualTo("hi");
        assertThat(result.status()).as(result.verification().problems().toString()).isEqualTo("answered");
        assertThat(result.language()).isEqualTo("hi");
        assertThat(result.warnings()).singleElement().satisfies(w -> {
            assertThat(w.code()).isEqualTo("AMENDMENT_2_5_C");
            assertThat(w.message()).contains("2(5)(c)");
            assertThat(w.link()).startsWith("https://teamleaseregtech.com/");
        });
    }

    @Test
    void explicitLanguageOverridesDetection() {
        String[] languageSent = new String[1];
        withAi(req -> {
            languageSent[0] = req.language();
            return new Proposal("not_found", req.language(), "", List.of(), List.of(), "fake");
        }).ask("What is the fee for building permission for a residential house?", "hi");
        assertThat(languageSent[0]).isEqualTo("hi");
    }

    @Test
    void needsMoreInfoKeepsMissingInputs() {
        AnswerService.AskResult result = withAi(req -> new Proposal("needs_more_info", "en",
                "What is the width of the road in front of the plot?", List.of(), List.of("road width"), "fake"))
                .ask("How much open space do I leave at the front?", "auto");
        assertThat(result.status()).isEqualTo("needs_more_info");
        assertThat(result.missingInputs()).containsExactly("road width");
        assertThat(result.warnings()).isEmpty(); // no plot area given, no Rule 2(5) involved
    }
}

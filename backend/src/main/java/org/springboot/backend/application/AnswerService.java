package org.springboot.backend.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springboot.backend.citation.AnswerVerifier;
import org.springboot.backend.integration.ai.AiAnswerClient;
import org.springboot.backend.retrieval.QueryIntent;
import org.springboot.backend.retrieval.Retriever;
import org.springboot.backend.retrieval.Topic;
import org.springboot.backend.rules.Chunk;
import org.springboot.backend.rules.RulesVersion;
import org.springframework.stereotype.Service;

/**
 * Answers a clerk's question: retrieve rule chunks, ask the AI service for a short grounded
 * answer, verify every quote and number against the rule text (retrying once), then build
 * citations from the backend's own chunk metadata (rule number, title, pages). The model only
 * supplies the chunk ID and the quote. An answer that cannot be verified becomes "not_found".
 */
@Service
public class AnswerService {

    private static final Set<String> STATUSES = Set.of("answered", "not_found", "needs_more_info");
    /** Plots up to this size are affected by the 28 Aug 2026 amendment to Rule 2(5)(c). */
    private static final double AMENDED_PLOT_LIMIT_SQM = 500.0;

    private static final Logger log = LoggerFactory.getLogger(AnswerService.class);

    private final Retriever retriever;
    private final AiAnswerClient ai;
    private final AnswerVerifier verifier;

    public AnswerService(Retriever retriever, AiAnswerClient ai, AnswerVerifier verifier) {
        this.retriever = retriever;
        this.ai = ai;
        this.verifier = verifier;
    }

    /** @param requestedLanguage "auto", "en" or "hi" */
    public AskResult ask(String question, String requestedLanguage) {
        Retriever.Result retrieved = retriever.retrieve(question);
        QueryIntent intent = retrieved.intent();
        String language = "en".equals(requestedLanguage) || "hi".equals(requestedLanguage)
                ? requestedLanguage
                : intent.language();

        Map<String, Chunk> chunksById = new LinkedHashMap<>();
        List<AiAnswerClient.ChunkText> context = new ArrayList<>();
        for (Retriever.RetrievedChunk r : retrieved.chunks()) {
            Chunk c = r.chunk();
            chunksById.put(c.id(), c);
            context.add(new AiAnswerClient.ChunkText(c.id(), c.label(), c.title(), pages(c), c.text()));
        }

        // Numbers the answer may legitimately mention besides those in the cited rules.
        StringBuilder trustedText = new StringBuilder(question).append('\n').append(RulesVersion.VERSION);
        chunksById.values().forEach(c -> trustedText.append(' ').append(pages(c)));

        // Ask, verify every claim against the rule text, retry once, then fail closed.
        AiAnswerClient.Request request = new AiAnswerClient.Request(question, language, context, null);
        AiAnswerClient.Proposal proposal;
        AnswerVerifier.Result check;
        int attempts = 0;
        do {
            attempts++;
            proposal = ai.answer(request);
            if (!STATUSES.contains(proposal.status())) {
                proposal = new AiAnswerClient.Proposal("not_found", language, "", List.of(), List.of(), proposal.model());
            }
            check = verifier.verify(proposal, chunksById, trustedText.toString());
            if (!check.passed()) {
                log.warn("Answer failed verification (attempt {}): {}", attempts, check.problems());
            }
        } while (!check.passed() && attempts < 2);
        Verification verification = new Verification(check.passed(), attempts, check.problems());

        if (!check.passed()) {
            return new AskResult("not_found", language, unverifiedMessage(language), List.of(), List.of(), null,
                    RulesVersion.VERSION, warnings(intent, List.of(), language), verification);
        }

        String status = proposal.status();
        List<Citation> citations = new ArrayList<>();
        if (!"not_found".equals(status)) {
            for (AnswerVerifier.VerifiedCitation v : check.citations()) {
                Chunk c = v.chunk();
                citations.add(new Citation(c.rule(), c.subRule(), c.title(), pages(c), c.pageStart(), v.quote()));
            }
        }
        String answer = "not_found".equals(status) && proposal.answer().isBlank() ? notFoundMessage(language) : proposal.answer();
        List<String> missing = "needs_more_info".equals(status) ? proposal.missingInputs() : List.of();

        return new AskResult(status, language, answer, List.copyOf(citations), missing, null,
                RulesVersion.VERSION, warnings(intent, citations, language), verification);
    }

    /** Warns when the 28 Aug 2026 amendment to Rule 2(5)(c), which is not in the loaded text, may apply. */
    static List<Warning> warnings(QueryIntent intent, List<Citation> citations, String language) {
        boolean smallPlot = intent.plotAreaSqm() != null && intent.plotAreaSqm() <= AMENDED_PLOT_LIMIT_SQM;
        boolean namesRule = intent.ruleRefs().stream().anyMatch(r -> r.replace(" ", "").startsWith("2(5)"));
        boolean aboutPermission = intent.topics().contains(Topic.DEVELOPMENT_PERMISSION);
        boolean citesRule = citations.stream().anyMatch(c -> c.rule().equals("2") && c.subRule().startsWith("(5)"));
        if (!(smallPlot || namesRule || aboutPermission || citesRule)) {
            return List.of();
        }
        String message = "hi".equals(language) ? RulesVersion.AMENDMENT_WARNING_HI : RulesVersion.AMENDMENT_WARNING;
        return List.of(new Warning("AMENDMENT_2_5_C", message, RulesVersion.AMENDMENT_SOURCE));
    }

    private static String unverifiedMessage(String language) {
        return "hi".equals(language)
                ? "नियमों के पाठ से सत्यापित उत्तर नहीं मिला। कृपया विभाग से पुष्टि करें।"
                : "No answer could be verified against the rule text. Please check with the department.";
    }

    private static String notFoundMessage(String language) {
        return "hi".equals(language)
                ? "लोड किए गए नियमों में इसका उत्तर नहीं मिला। कृपया विभाग से पुष्टि करें।"
                : "Not found in the loaded rules. Please check with the department.";
    }

    private static String pages(Chunk c) {
        return c.pageStart() == c.pageEnd() ? String.valueOf(c.pageStart()) : c.pageStart() + "-" + c.pageEnd();
    }

    /**
     * @param status        "answered", "not_found" or "needs_more_info"
     * @param calc          calculator result (stage 5); null when no calculator applied
     */
    public record AskResult(
            String status,
            String language,
            String answer,
            List<Citation> citations,
            List<String> missingInputs,
            Object calc,
            String rulesVersion,
            List<Warning> warnings,
            Verification verification) {
    }

    /**
     * Outcome of checking the AI's answer against the rule text.
     *
     * @param attempts 1, or 2 when the first answer failed and was retried
     * @param problems why the last attempt failed; empty when it passed
     */
    public record Verification(boolean passed, int attempts, List<String> problems) {
    }

    /** @param pdfPages e.g. "77" or "36-37"; pageStart is for the "Open PDF page" link */
    public record Citation(String rule, String subRule, String title, String pdfPages, int pageStart, String quote) {
    }

    public record Warning(String code, String message, String link) {
    }
}

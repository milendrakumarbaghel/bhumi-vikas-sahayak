package org.springboot.backend.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springboot.backend.dto.AskQuestionRequest;
import org.springboot.backend.dto.AskQuestionResponse;
import org.springboot.backend.dto.CitationResponse;
import org.springboot.backend.entity.RulePassage;
import org.springboot.backend.service.AiService;
import org.springboot.backend.service.AmendmentService;
import org.springboot.backend.service.AssistantService;
import org.springboot.backend.service.CalculationService;
import org.springboot.backend.service.CitationValidationService;
import org.springboot.backend.service.RuleRetrievalService;
import org.springframework.stereotype.Service;

@Service
public class AssistantServiceImpl implements AssistantService {

    private static final String SOURCE_VERSION = "MP Bhumi Vikas Rules, 2012; consolidated through 6 March 2025";
    private static final String SOURCE_DOCUMENT_URL = "/api/v1/documents/rules.pdf";
    private static final String DISCLAIMER =
            "Information support only; the competent authority decides applicability and approval.";

    private final AiService ai;
    private final RuleRetrievalService retrieval;
    private final CalculationService calculations;
    private final CitationValidationService citations;
    private final AmendmentService amendments;

    public AssistantServiceImpl(
            AiService ai,
            RuleRetrievalService retrieval,
            CalculationService calculations,
            CitationValidationService citations,
            AmendmentService amendments) {
        this.ai = ai;
        this.retrieval = retrieval;
        this.calculations = calculations;
        this.citations = citations;
        this.amendments = amendments;
    }

    @Override
    public AskQuestionResponse answer(AskQuestionRequest request) {
        LocalDate asOfDate = request.asOfDate() == null
                ? LocalDate.now(ZoneId.of("Asia/Kolkata"))
                : request.asOfDate();
        AiService.Analysis analysis = ai.analyse(request.question());
        String language = responseLanguage(request.language(), analysis.language());
        List<RulePassage> matches = retrieval.retrieve(
                analysis.searchTerms() + " " + analysis.englishQuestion(),
                analysis.ruleReferences());
        if (matches.isEmpty()) {
            return new AskQuestionResponse(
                    UUID.randomUUID(), "NOT_FOUND_IN_RULES", language, asOfDate,
                    SOURCE_VERSION, SOURCE_DOCUMENT_URL, notFound(language), List.of(), Map.of(),
                    List.of("No matching provision was found in the corpus consolidated through 6 March 2025."),
                    DISCLAIMER);
        }

        List<CitationResponse> citationResponses = matches.stream()
                .map(this::citation)
                .toList();
        Map<String, Object> calculation = calculations.calculate(analysis);
        List<String> warnings = new ArrayList<>();
        warnings.add("Source corpus is consolidated through 6 March 2025.");
        boolean amendmentReview = amendments.needsAugust2026Review(analysis.ruleReferences(), asOfDate);
        if (amendmentReview) {
            warnings.add("Rule 2(5)(c) has a known later amendment whose authoritative text is not loaded.");
        }

        String status = amendmentReview ? "NEEDS_REVIEW" : "ANSWERED";
        String answer = summarize(language, matches, calculation);
        return new AskQuestionResponse(
                UUID.randomUUID(), status, language, asOfDate, SOURCE_VERSION, SOURCE_DOCUMENT_URL, answer,
                citationResponses, calculation, List.copyOf(warnings), DISCLAIMER);
    }

    private CitationResponse citation(RulePassage passage) {
        String excerpt = citations.excerpt(passage, 500);
        return new CitationResponse(
                passage.getRule().getRuleNumber(),
                passage.getRule().getTitle(),
                passage.getLabel(),
                passage.getPageStart(),
                passage.getPageEnd(),
                SOURCE_DOCUMENT_URL + "#page=" + passage.getPageStart(),
                excerpt,
                citations.isValid(passage, excerpt));
    }

    private static String responseLanguage(String requested, String detected) {
        if (requested != null && (requested.equalsIgnoreCase("hi") || requested.equalsIgnoreCase("en"))) {
            return requested.toLowerCase(java.util.Locale.ROOT);
        }
        return "hi".equalsIgnoreCase(detected) ? "hi" : "en";
    }

    private static String summarize(
            String language,
            List<RulePassage> matches,
            Map<String, Object> calculation) {
        String rules = matches.stream()
                .limit(3)
                .map(match -> match.getRule().getRuleNumber())
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        if ("hi".equals(language)) {
            return calculation.isEmpty()
                    ? "उपलब्ध नियमों में संबंधित प्रावधान मिले हैं: नियम " + rules + "। नीचे दिए मूल पाठ की जाँच करें।"
                    : "नियम " + rules + " के आधार पर सत्यापित गणना नीचे दी गई है।";
        }
        return calculation.isEmpty()
                ? "Relevant provisions were found in Rule(s) " + rules + ". Review the cited source text below."
                : "A deterministic calculation was produced from Rule(s) " + rules + ".";
    }

    private static String notFound(String language) {
        return "hi".equals(language)
                ? "उपलब्ध भूमि विकास नियमों में कोई लागू प्रावधान नहीं मिला।"
                : "No applicable provision was found in the available Bhumi Vikas Rules.";
    }
}

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
import org.springboot.backend.integration.ai.GroundedAiClient;
import org.springboot.backend.service.AiService;
import org.springboot.backend.service.AmendmentService;
import org.springboot.backend.service.AssistantService;
import org.springboot.backend.service.CalculationService;
import org.springboot.backend.service.CitationValidationService;
import org.springboot.backend.service.GeneratedAnswerValidationService;
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
    private final GroundedAiClient groundedAi;
    private final GeneratedAnswerValidationService generatedAnswers;

    public AssistantServiceImpl(
            AiService ai,
            RuleRetrievalService retrieval,
            CalculationService calculations,
            CitationValidationService citations,
            AmendmentService amendments,
            GroundedAiClient groundedAi,
            GeneratedAnswerValidationService generatedAnswers) {
        this.ai = ai;
        this.retrieval = retrieval;
        this.calculations = calculations;
        this.citations = citations;
        this.amendments = amendments;
        this.groundedAi = groundedAi;
        this.generatedAnswers = generatedAnswers;
    }

    @Override
    public AskQuestionResponse answer(AskQuestionRequest request) {
        LocalDate asOfDate = request.asOfDate() == null
                ? LocalDate.now(ZoneId.of("Asia/Kolkata"))
                : request.asOfDate();
        AiService.Analysis analysis = ai.analyse(request.question());
        String language = responseLanguage(request.language(), analysis.language());
        List<RulePassage> baseMatches = retrieval.retrieve(
                analysis.searchTerms() + " " + analysis.englishQuestion(),
                analysis.ruleReferences());
        List<RulePassage> matches = amendments.resolve(baseMatches,request.question(),analysis.ruleReferences(),asOfDate);
        if (matches.isEmpty()) {
            return new AskQuestionResponse(
                    UUID.randomUUID(), "NOT_FOUND_IN_RULES", language, asOfDate,
                    SOURCE_VERSION, SOURCE_DOCUMENT_URL, notFound(language), List.of(), Map.of(),
                    List.of("No matching provision was found in the corpus consolidated through 6 March 2025."),
                    DISCLAIMER);
        }

        Map<String, Object> calculation = groundedAi.calculate(analysis)
                .orElseGet(() -> calculations.calculate(analysis));
        List<String> warnings = new ArrayList<>();
        warnings.add("Source corpus is consolidated through 6 March 2025.");
        if (matches.stream().anyMatch(p -> p.getSourceVersion()!=null)) {
            // Calculator tables belong to the March 2025 corpus, not replacement notifications.
            calculation=Map.of("calculations",List.of(),"notApplicable",List.of("Base calculator disabled for effective-dated replacement text"),"missingInputs",List.of());
            warnings.add("Effective-dated amendment text applied; base-corpus calculators are withheld for this answer.");
        }
        boolean amendmentReview = amendments.needsAugust2026Review(analysis.ruleReferences(), asOfDate);
        if (amendmentReview) {
            warnings.add("Rule 2(5)(c) has a known later amendment whose authoritative text is not loaded.");
        }

        String status = amendmentReview ? "NEEDS_REVIEW" : "ANSWERED";
        if(!amendmentReview && calculation.get("missingInputs") instanceof List<?> missing && !missing.isEmpty()) {
            status="NEEDS_MORE_INFORMATION";
            warnings.add("Required inputs: "+missing);
        }
        String answer = summarize(language, matches, calculation,request.question());
        List<CitationResponse> citationResponses = matches.stream()
                .map(passage -> citation(passage, request.question()))
                .toList();

        var proposal = groundedAi.answer(request.question(), language, matches, calculation);
        if (proposal.isPresent()) {
            var check = generatedAnswers.validate(proposal.get(), matches, request.question(), calculation);
            if (check.passed() && !"not_found".equals(proposal.get().status())
                    && groundedAi.verifyClaims(proposal.get(),calculation,request.question())) {
                answer = proposal.get().answer();
                citationResponses = check.citations().stream().map(this::citation).toList();
                if (!amendmentReview && "needs_more_info".equals(proposal.get().status())) {
                    status = "NEEDS_MORE_INFORMATION";
                    warnings.add("Required inputs: " + String.join(", ", proposal.get().missingInputs()));
                }
            } else {
                warnings.add("Generated wording failed source validation; a deterministic fallback is shown.");
            }
        }
        String sourceVersion=matches.stream().filter(p -> p.getSourceVersion()!=null)
                .map(RulePassage::getSourceVersion).distinct().reduce(SOURCE_VERSION,(a,b)->a+"; "+b);
        return new AskQuestionResponse(
                UUID.randomUUID(), status, language, asOfDate, sourceVersion, SOURCE_DOCUMENT_URL, answer,
                citationResponses, calculation, List.copyOf(warnings), DISCLAIMER);
    }

    private CitationResponse citation(RulePassage passage, String question) {
        String excerpt = citations.relevantExcerpt(passage, question, 500);
        return new CitationResponse(
                passage.getRule().getRuleNumber(),
                passage.getRule().getTitle(),
                passage.getLabel(),
                passage.getPageStart(),
                passage.getPageEnd(),
                sourceUrl(passage),
                excerpt,
                citations.isValid(passage, excerpt));
    }

    private CitationResponse citation(GeneratedAnswerValidationService.VerifiedCitation citation) {
        RulePassage passage = citation.passage();
        return new CitationResponse(
                passage.getRule().getRuleNumber(), passage.getRule().getTitle(), passage.getLabel(),
                passage.getPageStart(), passage.getPageEnd(),
                sourceUrl(passage),
                citation.quote(), true);
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
            Map<String, Object> calculation,String question) {
        String rules = matches.stream()
                .limit(3)
                .map(match -> match.getRule().getRuleNumber())
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        String prefix="hi".equals(language)?"नियम "+rules+" के संबंधित मूल प्रावधान (अंग्रेज़ी):":"Relevant source provisions in Rule(s) "+rules+":";
        String evidence=matches.stream().limit(3).map(p -> new CitationValidationService().relevantExcerpt(p,question,1800))
                .collect(java.util.stream.Collectors.joining("\n\n"));
        return prefix+"\n"+evidence;
    }

    private static String sourceUrl(RulePassage passage) {
        return (passage.getSourceUrl()==null?SOURCE_DOCUMENT_URL:passage.getSourceUrl())+"#page="+passage.getPageStart();
    }

    private static String notFound(String language) {
        return "hi".equals(language)
                ? "उपलब्ध भूमि विकास नियमों में कोई लागू प्रावधान नहीं मिला।"
                : "No applicable provision was found in the available Bhumi Vikas Rules.";
    }
}

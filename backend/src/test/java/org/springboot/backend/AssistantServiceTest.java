package org.springboot.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springboot.backend.dto.AskQuestionRequest;
import org.springboot.backend.entity.BuildingRule;
import org.springboot.backend.entity.RulePassage;
import org.springboot.backend.service.AiService;
import org.springboot.backend.service.AmendmentService;
import org.springboot.backend.service.CalculationService;
import org.springboot.backend.service.CitationValidationService;
import org.springboot.backend.service.RuleRetrievalService;
import org.springboot.backend.service.impl.AssistantServiceImpl;
import org.junit.jupiter.api.Test;

class AssistantServiceTest {

    @Test
    void returnsCalculatedAnswerWithValidatedCitation() {
        AiService ai = mock(AiService.class);
        RuleRetrievalService retrieval = mock(RuleRetrievalService.class);
        AmendmentService amendments = mock(AmendmentService.class);
        BuildingRule rule = new BuildingRule("56", "Residential Buildings", "Part V", 77, 80, "full text");
        RulePassage passage = new RulePassage(
                "Rule 56(1)(a)", 77, 77,
                "More than 9.0 meters and up to 12 meters 3.6 meters");
        rule.addPassage(passage);
        AiService.Analysis analysis = new AiService.Analysis(
                "en", "front open space", "front open space", List.of("SETBACK"),
                null, 10.0, 9.0, null, "residential", List.of("56"), "offline");
        when(ai.analyse(any())).thenReturn(analysis);
        when(retrieval.retrieve(any(), any())).thenReturn(List.of(passage));
        when(amendments.needsAugust2026Review(any(), any())).thenReturn(false);

        var service = new AssistantServiceImpl(
                ai, retrieval, new CalculationService(), new CitationValidationService(), amendments);
        var response = service.answer(new AskQuestionRequest(
                "My house faces a 10 m road and is 9 m high. What setback applies?",
                "en", LocalDate.of(2026, 10, 11)));

        assertThat(response.status()).isEqualTo("ANSWERED");
        assertThat(response.sourceVersion()).contains("6 March 2025");
        assertThat(response.sourceDocumentUrl()).isEqualTo("/api/v1/documents/rules.pdf");
        assertThat(response.calculation())
                .containsEntry("code", "RESIDENTIAL_FRONT_OPEN_SPACE")
                .containsEntry("frontOpenSpaceMeters", java.math.BigDecimal.valueOf(3.6));
        assertThat(response.citations()).singleElement().satisfies(citation -> {
            assertThat(citation.ruleNumber()).isEqualTo("56");
            assertThat(citation.sourceUrl()).endsWith("#page=77");
            assertThat(citation.validated()).isTrue();
        });
    }

    @Test
    void returnsNotFoundWithoutInventingCitation() {
        AiService ai = mock(AiService.class);
        RuleRetrievalService retrieval = mock(RuleRetrievalService.class);
        AmendmentService amendments = mock(AmendmentService.class);
        when(ai.analyse(any())).thenReturn(new AiService.Analysis(
                "en", "property tax", "property tax", List.of(),
                null, null, null, null, null, List.of(), "offline"));
        when(retrieval.retrieve(any(), any())).thenReturn(List.of());

        var service = new AssistantServiceImpl(
                ai, retrieval, new CalculationService(), new CitationValidationService(), amendments);
        var response = service.answer(new AskQuestionRequest(
                "How do I pay property tax online?", "hi", LocalDate.of(2026, 10, 11)));

        assertThat(response.status()).isEqualTo("NOT_FOUND_IN_RULES");
        assertThat(response.language()).isEqualTo("hi");
        assertThat(response.answer()).contains("कोई लागू प्रावधान नहीं मिला");
        assertThat(response.sourceVersion()).contains("6 March 2025");
        assertThat(response.citations()).isEmpty();
        assertThat(response.calculation()).isEqualTo(Map.of());
    }
}

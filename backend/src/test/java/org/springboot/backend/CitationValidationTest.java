package org.springboot.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.springboot.backend.entity.RulePassage;
import org.springboot.backend.service.CitationValidationService;
import org.junit.jupiter.api.Test;

class CitationValidationTest {

    private final CitationValidationService validator = new CitationValidationService();

    @Test
    void validatesOnlyExactNormalizedSourceText() {
        RulePassage passage = new RulePassage(
                "Rule 56(1)(a)", 77, 77,
                "More than 9.0 meters and up to 12 meters     3.6 meters");

        assertThat(validator.isValid(
                passage, "More than 9.0 meters and up to 12 meters 3.6 meters")).isTrue();
        assertThat(validator.isValid(passage, "The setback is 4.0 meters")).isFalse();
    }

    @Test
    void generatedExcerptRemainsVerifiable() {
        RulePassage passage = new RulePassage("Rule 21", 35, 38, "A".repeat(600));
        String excerpt = validator.excerpt(passage, 500);

        assertThat(excerpt).hasSize(500);
        assertThat(validator.isValid(passage, excerpt)).isTrue();
    }
}

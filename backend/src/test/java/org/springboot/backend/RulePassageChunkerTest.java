package org.springboot.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springboot.backend.util.RulePassageChunker;

class RulePassageChunkerTest {

    @Test
    void keepsATableInsideItsNumberedSubRuleAndTracksPage() {
        String text = "56. Residential Buildings\n(1) Front open spaces\nWidth Result\n9 3.0\n"
                + "Rule 56    M.P. Rules    78\n(2) Rear open spaces\nArea Result\n150 1.5";

        var passages = new RulePassageChunker().chunk("56", 77, 78, text);

        assertThat(passages).extracting(RulePassageChunker.Passage::label)
                .contains("Rule 56 (1)", "Rule 56 (2)");
        assertThat(passages).filteredOn(value -> value.label().equals("Rule 56 (1)"))
                .singleElement().satisfies(value -> assertThat(value.text()).contains("Width Result", "9 3.0"));
        assertThat(passages).filteredOn(value -> value.label().equals("Rule 56 (2)"))
                .singleElement().satisfies(value -> assertThat(value.pageStart()).isEqualTo(78));
    }
}

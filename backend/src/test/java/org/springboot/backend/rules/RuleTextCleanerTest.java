package org.springboot.backend.rules;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RuleTextCleanerTest {

    @Test
    void removesRunningHeadersAndTracksPages() {
        String raw = """
                56. Residential Buildings.-Open Spaces
                     (1) Front open spaces
                Rule 56                           M.P. Bhumi Vikas Rules, 2012                       78
                     (2) Rear Open Space
                """;
        RuleTextCleaner.CleanedText cleaned = RuleTextCleaner.clean(raw, 77);

        assertThat(cleaned.text()).doesNotContain("M.P. Bhumi Vikas Rules, 2012");
        assertThat(cleaned.pageAt(cleaned.text().indexOf("Front"))).isEqualTo(77);
        assertThat(cleaned.pageAt(cleaned.text().indexOf("Rear"))).isEqualTo(78);
    }

    @Test
    void removesFootnoteMarkersButKeepsAmendedText() {
        String raw = """
                12 A. For residential plots up to 1[186 sqm], instant approval
                     1
                     [17-a. "Agricultural tourist facility"
                """;
        String text = RuleTextCleaner.clean(raw, 1).text();

        assertThat(text).contains("up to [186 sqm]");
        assertThat(text).doesNotContainPattern("(?m)^[ \\t]*1[ \\t]*$");
        assertThat(text).contains("[17-a. \"Agricultural tourist facility\"");
    }

    @Test
    void keepsTableSpacing() {
        String raw = "    1.    Up to 9.0 meters                                            3.0 meters\n";
        assertThat(RuleTextCleaner.clean(raw, 1).text()).isEqualTo(raw.strip());
    }
}

package org.springboot.backend.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RuleRepositoryTest {

    static RuleRepository repo;

    @BeforeAll
    static void load() {
        repo = new RuleRepository(Path.of("../track-1-bhumi-vikas-sahayak/data/bhumi_vikas_rules.jsonl"));
    }

    @Test
    void loadsAll106Rules() {
        assertThat(repo.all()).hasSize(106);
        assertThat(repo.find("26-B")).isPresent();
        assertThat(repo.find("Rule 26b").map(Rule::number)).contains("26-B");
        assertThat(repo.find("999")).isEmpty();
    }

    @Test
    void noRunningHeadersLeft() {
        for (Rule rule : repo.all()) {
            assertThat(rule.text())
                    .as("rule " + rule.number())
                    .doesNotContainPattern("(?m)^[ \\t]*Rule\\s+\\S.*M\\.P\\. Bhumi Vikas Rules");
        }
    }

    @Test
    void findsAllSixInsertedRules() {
        assertThat(repo.findInserted("17-a").map(InsertedRule::parentRule)).contains("17");
        assertThat(repo.find("17-a").map(Rule::number)).contains("17");
        for (String id : List.of("12-A", "17-a", "25(A)", "26-A", "42-A", "93(a)")) {
            assertThat(repo.findInserted(id)).as(id).isPresent();
        }
        // 26-A starts on PDF page 47 (checked in the PDF)
        assertThat(repo.findInserted("26-A").get().page()).isEqualTo(47);
    }

    @Test
    void insertedRuleIsItsOwnChunk() {
        assertThat(repo.chunks()).anySatisfy(c -> {
            assertThat(c.rule()).isEqualTo("17");
            assertThat(c.subRule()).isEqualTo("17-a");
            assertThat(c.text()).contains("Agricultural tourist facility");
            assertThat(c.label()).isEqualTo("Rule 17 (17-a)");
        });
    }

    @Test
    void frontOpenSpaceTableIsNotSplit() {
        List<Chunk> withTable = repo.chunks().stream()
                .filter(c -> c.rule().equals("56") && c.text().contains("Up to 9.0 meters"))
                .toList();
        assertThat(withTable).hasSize(1);
        Chunk chunk = withTable.get(0);
        assertThat(chunk.text()).contains("3.0 meters", "3.6 meters", "4.5 meters", "Above 18 meters");
        assertThat(chunk.containsTable()).isTrue();
        assertThat(chunk.pageStart()).isEqualTo(77);
    }

    @Test
    void residentialFeeTableIsNotSplit() {
        List<Chunk> withTable = repo.chunks().stream()
                .filter(c -> c.rule().equals("21") && c.text().contains("exclusively as residence"))
                .toList();
        assertThat(withTable).isNotEmpty();
        assertThat(withTable).anySatisfy(c -> assertThat(c.text()).contains("0          75         750", "above 2500"));
    }

    @Test
    void chunkLabelsNameTheSubRule() {
        assertThat(repo.chunks()).anySatisfy(c -> {
            assertThat(c.label()).isEqualTo("Rule 2 (5)(c)");
            assertThat(c.text()).contains("already developed in the Central area");
            assertThat(c.pageStart()).isEqualTo(3);
        });
        assertThat(repo.chunks()).anySatisfy(c -> {
            assertThat(c.label()).isEqualTo("Rule 21 (3)(b)");
            assertThat(c.pageStart()).isEqualTo(36);
        });
        // Nested lists like "Explanation (1)" must not produce labels such as "(21)-(1)"
        assertThat(repo.chunks()).noneSatisfy(c -> assertThat(c.subRule()).matches(".*\\((\\d+)\\)-\\(1\\).*"));
    }

    @Test
    void everyChunkHasValidPages() {
        for (Chunk c : repo.chunks()) {
            Rule rule = repo.find(c.rule()).orElseThrow();
            assertThat(c.pageStart()).as(c.id()).isBetween(rule.pageStart(), rule.pageEnd());
            assertThat(c.pageEnd()).as(c.id()).isBetween(c.pageStart(), rule.pageEnd());
        }
    }
}

package org.springboot.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.springboot.backend.entity.BuildingRule;
import org.springboot.backend.entity.RulePassage;
import org.springboot.backend.repository.BuildingRuleRepository;
import org.springboot.backend.repository.RulePassageRepository;
import org.springboot.backend.service.RuleRetrievalService;
import org.springboot.backend.util.JsonlParser;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class RuleRetrievalServiceTest {

    @Test
    void retrievesAndDeduplicatesMatchingPassages() {
        BuildingRuleRepository rules = mock(BuildingRuleRepository.class);
        RulePassageRepository passages = mock(RulePassageRepository.class);
        BuildingRule rule = new BuildingRule("56", "Residential Buildings", "Part V", 77, 80, "full text");
        RulePassage passage = new RulePassage("Rule 56(1)(a)", 77, 77, "front open space road width");
        rule.addPassage(passage);
        when(passages.search("front")).thenReturn(List.of(passage));
        when(passages.search("space")).thenReturn(List.of(passage));

        List<RulePassage> result = new RuleRetrievalService(rules, passages)
                .retrieve("front space", List.of());

        assertThat(result).containsExactly(passage);
    }

    @Test
    void bundledCorpusContainsAll106TopLevelRecords() throws Exception {
        try (var input = getClass().getResourceAsStream("/data/bhumi_vikas_rules.jsonl")) {
            assertThat(input).isNotNull();
            var records = new JsonlParser(JsonMapper.builder().build()).parse(input);
            assertThat(records).hasSize(106);
            assertThat(records).anySatisfy(rule -> assertThat(rule.rule()).isEqualTo("26-B"));
        }
    }
}

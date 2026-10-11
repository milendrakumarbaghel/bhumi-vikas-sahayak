package org.springboot.backend.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springboot.backend.entity.RulePassage;
import org.springboot.backend.entity.BuildingRule;
import org.springboot.backend.exception.RuleNotFoundException;
import org.springboot.backend.repository.BuildingRuleRepository;
import org.springboot.backend.repository.RulePassageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuleRetrievalService {

    private static final int MAX_RESULTS = 8;
    private final BuildingRuleRepository rules;
    private final RulePassageRepository passages;

    public RuleRetrievalService(BuildingRuleRepository rules, RulePassageRepository passages) {
        this.rules = rules;
        this.passages = passages;
    }

    @Transactional(readOnly = true)
    public BuildingRule rule(String ruleNumber) {
        BuildingRule rule = rules.findByRuleNumberIgnoreCase(ruleNumber)
                .orElseThrow(() -> new RuleNotFoundException(ruleNumber));
        rule.getPassages();
        return rule;
    }

    @Transactional(readOnly = true)
    public List<BuildingRule> rules() {
        List<BuildingRule> result = rules.findAll();
        result.forEach(BuildingRule::getPassages);
        return result;
    }

    @Transactional(readOnly = true)
    public List<RulePassage> retrieve(String searchText, List<String> ruleReferences) {
        Map<String, ScoredPassage> candidates = new LinkedHashMap<>();
        for (String reference : ruleReferences == null ? List.<String>of() : ruleReferences) {
            addMatches(candidates, reference, 100);
        }
        for (String term : terms(searchText)) {
            addMatches(candidates, term, score(term));
        }
        return candidates.values().stream()
                .sorted((left, right) -> Integer.compare(right.score(), left.score()))
                .limit(MAX_RESULTS)
                .map(ScoredPassage::passage)
                .toList();
    }

    private void addMatches(Map<String, ScoredPassage> candidates, String term, int score) {
        if (term == null || term.isBlank()) {
            return;
        }
        for (RulePassage passage : passages.search(term.strip())) {
            String key = passage.getId() == null
                    ? passage.getRule().getRuleNumber() + ":" + passage.getLabel()
                    : passage.getId().toString();
            candidates.merge(
                    key,
                    new ScoredPassage(passage, score),
                    (oldValue, newValue) -> new ScoredPassage(passage, oldValue.score() + score));
        }
    }

    private static List<String> terms(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String raw : text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}()-]+")) {
            if (raw.length() >= 3) {
                result.add(raw);
            }
        }
        return result.stream().distinct().limit(20).toList();
    }

    private static int score(String term) {
        return Math.min(20, Math.max(1, term.length()));
    }

    private record ScoredPassage(RulePassage passage, int score) {
    }
}

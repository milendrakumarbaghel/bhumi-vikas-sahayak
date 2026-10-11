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
import org.springboot.backend.util.QueryLexicon;
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
        if (QueryLexicon.outsideCorpus(searchText)) return List.of();
        Map<String, ScoredPassage> candidates = new LinkedHashMap<>();
        List<String> tokens = QueryLexicon.terms(searchText);
        String fullTextQuery = String.join(" OR ",tokens.stream().filter(t -> t.matches("[a-z]+") ).toList());
        if (!fullTextQuery.isBlank()) {
            List<Long> ids = passages.rankedSearch(fullTextQuery);
            if (!ids.isEmpty()) for (RulePassage passage : passages.fetchRanked(ids)) {
                int rank = ids.indexOf(passage.getId());
                merge(candidates,passage,80-rank);
            }
        }
        int referenceScore=5000;
        for (String reference : ruleReferences == null ? List.<String>of() : ruleReferences) {
            String base = reference.split("\\(",2)[0];
            for (RulePassage passage : passages.forRule(base)) {
                String label=passage.getLabel().replaceAll("(?i)rule|\\s","");
                String wanted=reference.replaceAll("\\s","");
                boolean precise = wanted.contains("(") && (label.startsWith(wanted) || wanted.startsWith(label+"("));
                merge(candidates,passage,referenceScore+(precise?500:0));
            }
            referenceScore=Math.max(500,referenceScore-1000);
        }
        for (String term : tokens) {
            addMatches(candidates, term, score(term));
        }
        return candidates.values().stream()
                .sorted(java.util.Comparator.comparingInt(ScoredPassage::score).reversed()
                        .thenComparing(value -> value.passage().getLabel()))
                .limit(MAX_RESULTS)
                .map(ScoredPassage::passage)
                .toList();
    }

    private void addMatches(Map<String, ScoredPassage> candidates, String term, int score) {
        if (term == null || term.isBlank()) {
            return;
        }
        for (RulePassage passage : passages.search(term.strip())) {
            merge(candidates,passage,score);
        }
    }

    private static void merge(Map<String,ScoredPassage> candidates,RulePassage passage,int score) {
            String key = passage.getId() == null
                    ? passage.getRule().getRuleNumber() + ":" + passage.getLabel()
                    : passage.getId().toString();
            candidates.merge(
                    key,
                    new ScoredPassage(passage, score),
                    (oldValue, newValue) -> new ScoredPassage(passage, oldValue.score() + score));
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

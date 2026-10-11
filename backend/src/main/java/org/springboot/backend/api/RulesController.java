package org.springboot.backend.api;

import java.util.List;
import java.util.Map;

import org.springboot.backend.application.RuleCatalogService;
import org.springboot.backend.rules.InsertedRule;
import org.springboot.backend.rules.Rule;
import org.springboot.backend.rules.RulesVersion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RulesController {

    private final RuleCatalogService catalogue;

    public RulesController(RuleCatalogService catalogue) {
        this.catalogue = catalogue;
    }

    /** Full cleaned text of one rule. Inserted identifiers return their parent with a focus. */
    @GetMapping("/rules/{ruleNumber}")
    public ResponseEntity<?> rule(@PathVariable String ruleNumber) {
        if (ruleNumber.length() > 20) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rule number too long"));
        }
        return catalogue.find(ruleNumber)
                .<ResponseEntity<?>>map(rule -> ResponseEntity.ok(RuleResponse.of(
                        rule,
                        catalogue.findInserted(ruleNumber).map(InsertedRule::id).orElse(null))))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of("error", "No rule " + ruleNumber + " in the loaded rules")));
    }

    @GetMapping("/rules")
    public List<Map<String, Object>> list() {
        return catalogue.all().stream()
                .map(rule -> Map.<String, Object>of(
                        "rule", rule.number(),
                        "title", rule.title(),
                        "pageStart", rule.pageStart()))
                .toList();
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "rulesLoaded", catalogue.all().size(),
                "chunks", catalogue.chunkCount(),
                "rulesVersion", RulesVersion.VERSION);
    }

    public record RuleResponse(
            String rule,
            String title,
            String part,
            int pageStart,
            int pageEnd,
            String text,
            List<InsertedRuleView> insertedRules,
            String focus,
            String rulesVersion) {

        static RuleResponse of(Rule rule, String focus) {
            List<InsertedRuleView> inserted = rule.insertedRules().stream()
                    .map(item -> new InsertedRuleView(
                            item.id(), item.title(), item.page(), item.startOffset()))
                    .toList();
            return new RuleResponse(
                    rule.number(), rule.title(), rule.part(), rule.pageStart(), rule.pageEnd(),
                    rule.text(), inserted, focus, RulesVersion.VERSION);
        }
    }

    public record InsertedRuleView(String id, String title, int page, int startOffset) {
    }
}

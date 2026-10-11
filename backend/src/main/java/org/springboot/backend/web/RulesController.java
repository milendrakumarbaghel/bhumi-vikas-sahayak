package org.springboot.backend.web;

import java.util.List;
import java.util.Map;

import org.springboot.backend.rules.InsertedRule;
import org.springboot.backend.rules.Rule;
import org.springboot.backend.rules.RuleRepository;
import org.springboot.backend.rules.RulesVersion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RulesController {

    private final RuleRepository rules;

    public RulesController(RuleRepository rules) {
        this.rules = rules;
    }

    /** Full cleaned text of one rule. "17-a" returns rule 17 with {@code focus} set to "17-a". */
    @GetMapping("/rules/{ruleNumber}")
    public ResponseEntity<?> rule(@PathVariable String ruleNumber) {
        if (ruleNumber.length() > 20) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rule number too long"));
        }
        return rules.find(ruleNumber)
                .<ResponseEntity<?>>map(rule -> ResponseEntity.ok(RuleResponse.of(rule,
                        rules.findInserted(ruleNumber).map(InsertedRule::id).orElse(null))))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "No rule " + ruleNumber + " in the loaded rules")));
    }

    /** Rule numbers and titles, for the UI. */
    @GetMapping("/rules")
    public List<Map<String, Object>> list() {
        return rules.all().stream()
                .map(r -> Map.<String, Object>of("rule", r.number(), "title", r.title(), "pageStart", r.pageStart()))
                .toList();
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "rulesLoaded", rules.all().size(),
                "chunks", rules.chunks().size(),
                "rulesVersion", RulesVersion.VERSION);
    }

    public record RuleResponse(
            String rule, String title, String part, int pageStart, int pageEnd,
            String text, List<InsertedRuleView> insertedRules, String focus, String rulesVersion) {

        static RuleResponse of(Rule r, String focus) {
            List<InsertedRuleView> inserted = r.insertedRules().stream()
                    .map(i -> new InsertedRuleView(i.id(), i.title(), i.page(), i.startOffset()))
                    .toList();
            return new RuleResponse(r.number(), r.title(), r.part(), r.pageStart(), r.pageEnd(),
                    r.text(), inserted, focus, RulesVersion.VERSION);
        }
    }

    public record InsertedRuleView(String id, String title, int page, int startOffset) {
    }
}

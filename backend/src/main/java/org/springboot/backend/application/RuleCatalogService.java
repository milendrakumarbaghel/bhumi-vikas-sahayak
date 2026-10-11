package org.springboot.backend.application;

import java.util.Collection;
import java.util.Optional;

import org.springboot.backend.rules.InsertedRule;
import org.springboot.backend.rules.Rule;
import org.springboot.backend.rules.RuleRepository;
import org.springframework.stereotype.Service;

/** Application boundary for rule catalogue reads. */
@Service
public class RuleCatalogService {

    private final RuleRepository rules;

    public RuleCatalogService(RuleRepository rules) {
        this.rules = rules;
    }

    public Optional<Rule> find(String ruleNumber) {
        return rules.find(ruleNumber);
    }

    public Optional<InsertedRule> findInserted(String ruleNumber) {
        return rules.findInserted(ruleNumber);
    }

    public Collection<Rule> all() {
        return rules.all();
    }

    public int chunkCount() {
        return rules.chunks().size();
    }
}

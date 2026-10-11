package org.springboot.backend.controller;

import java.util.List;

import org.springboot.backend.dto.RuleResponse;
import org.springboot.backend.entity.BuildingRule;
import org.springboot.backend.service.RuleImportService;
import org.springboot.backend.service.RuleRetrievalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final RuleRetrievalService retrieval;
    private final RuleImportService importer;

    public RuleController(RuleRetrievalService retrieval, RuleImportService importer) {
        this.retrieval = retrieval;
        this.importer = importer;
    }

    @GetMapping
    public List<RuleResponse> rules() {
        return retrieval.rules().stream().map(RuleController::response).toList();
    }

    @GetMapping("/{ruleNumber}")
    public RuleResponse rule(@PathVariable String ruleNumber) {
        return response(retrieval.rule(ruleNumber));
    }

    @PostMapping("/imports:run")
    public RuleImportService.ImportResult importRules() {
        return importer.importIfEmpty();
    }

    private static RuleResponse response(BuildingRule rule) {
        return new RuleResponse(
                rule.getId(), rule.getRuleNumber(), rule.getTitle(), rule.getPart(),
                rule.getPageStart(), rule.getPageEnd(), rule.getText(),
                rule.getPassages().stream()
                        .map(passage -> new RuleResponse.Passage(
                                passage.getId(), passage.getLabel(), passage.getPageStart(),
                                passage.getPageEnd(), passage.getText()))
                        .toList());
    }
}

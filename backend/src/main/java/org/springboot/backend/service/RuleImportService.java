package org.springboot.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import org.springboot.backend.entity.BuildingRule;
import org.springboot.backend.entity.RulePassage;
import org.springboot.backend.entity.SourceDocument;
import org.springboot.backend.repository.BuildingRuleRepository;
import org.springboot.backend.repository.SourceDocumentRepository;
import org.springboot.backend.util.JsonlParser;
import org.springboot.backend.util.RulePassageChunker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuleImportService {

    private final BuildingRuleRepository rules;
    private final SourceDocumentRepository documents;
    private final JsonlParser parser;
    private final RulePassageChunker chunker;
    private final Resource rulesResource;
    private final String pdfPath;

    public RuleImportService(
            BuildingRuleRepository rules,
            SourceDocumentRepository documents,
            JsonlParser parser,
            RulePassageChunker chunker,
            @Value("${bvs.rules-resource}") Resource rulesResource,
            @Value("${bvs.pdf-file}") String pdfPath) {
        this.rules = rules;
        this.documents = documents;
        this.parser = parser;
        this.chunker = chunker;
        this.rulesResource = rulesResource;
        this.pdfPath = pdfPath;
    }

    @Transactional
    public ImportResult importIfEmpty() {
        if (rules.count() > 0) {
            long rebuilt = rebuildLegacyPassages();
            return new ImportResult(rebuilt, rules.count(), rebuilt > 0);
        }
        if (documents.count() == 0) {
            documents.save(new SourceDocument(
                    "M.P. Bhumi Vikas Rules, 2012",
                    "EN",
                    "Town and Country Planning Department, Madhya Pradesh",
                    LocalDate.of(2025, 3, 6),
                    pdfPath));
        }
        try (InputStream input = rulesResource.getInputStream()) {
            var records = parser.parse(input);
            for (var record : records) {
                BuildingRule rule = new BuildingRule(
                        record.rule(), record.title(), record.part(), record.pageStart(), record.pageEnd(), record.text());
                addPassages(rule);
                rules.save(rule);
            }
            return new ImportResult(records.size(), rules.count(), true);
        } catch (IOException e) {
            throw new IllegalStateException("Could not open rules resource", e);
        }
    }

    private long rebuildLegacyPassages() {
        long rebuilt = 0;
        for (BuildingRule rule : rules.findAll()) {
            var expected=chunker.chunk(rule.getRuleNumber(),rule.getPageStart(),rule.getPageEnd(),rule.getText());
            if (rule.getPassages().size()!=expected.size() || java.util.stream.IntStream.range(0,expected.size())
                    .anyMatch(i -> !rule.getPassages().get(i).getText().equals(expected.get(i).text()))) {
                addPassages(rule);
                rules.save(rule);
                rebuilt++;
            }
        }
        return rebuilt;
    }

    private void addPassages(BuildingRule rule) {
        rule.replacePassages(chunker.chunk(
                        rule.getRuleNumber(), rule.getPageStart(), rule.getPageEnd(), rule.getText()).stream()
                .map(value -> new RulePassage(
                        value.label(), value.pageStart(), value.pageEnd(), value.text()))
                .toList());
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void importBundledRulesOnStartup() {
        importIfEmpty();
    }

    public record ImportResult(long imported, long total, boolean changed) {
    }
}

package org.springboot.backend.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "building_rule")
public class BuildingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_number", nullable = false, unique = true, length = 32)
    private String ruleNumber;

    @Column(nullable = false)
    private String title;

    @Column(name = "part_name", nullable = false)
    private String part;

    @Column(name = "page_start", nullable = false)
    private int pageStart;

    @Column(name = "page_end", nullable = false)
    private int pageEnd;

    @Column(name = "full_text", nullable = false, columnDefinition = "text")
    private String text;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RulePassage> passages = new ArrayList<>();

    protected BuildingRule() {
    }

    public BuildingRule(String ruleNumber, String title, String part, int pageStart, int pageEnd, String text) {
        this.ruleNumber = ruleNumber;
        this.title = title;
        this.part = part;
        this.pageStart = pageStart;
        this.pageEnd = pageEnd;
        this.text = text;
    }

    public void addPassage(RulePassage passage) {
        passage.attachTo(this);
        passages.add(passage);
    }

    public void replacePassages(List<RulePassage> replacements) {
        passages.clear();
        replacements.forEach(this::addPassage);
    }

    public Long getId() { return id; }
    public String getRuleNumber() { return ruleNumber; }
    public String getTitle() { return title; }
    public String getPart() { return part; }
    public int getPageStart() { return pageStart; }
    public int getPageEnd() { return pageEnd; }
    public String getText() { return text; }
    public List<RulePassage> getPassages() { return List.copyOf(passages); }
}

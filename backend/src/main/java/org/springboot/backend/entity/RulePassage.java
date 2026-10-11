package org.springboot.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "rule_passage")
public class RulePassage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private BuildingRule rule;

    @Column(nullable = false, length = 128)
    private String label;

    @Column(name = "page_start", nullable = false)
    private int pageStart;

    @Column(name = "page_end", nullable = false)
    private int pageEnd;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Transient
    private String sourceUrl;
    @Transient
    private String sourceVersion;

    public void withSource(String url,String version) { this.sourceUrl=url;this.sourceVersion=version; }
    public String getSourceUrl() { return sourceUrl; }
    public String getSourceVersion() { return sourceVersion; }

    protected RulePassage() {
    }

    public RulePassage(String label, int pageStart, int pageEnd, String text) {
        this.label = label;
        this.pageStart = pageStart;
        this.pageEnd = pageEnd;
        this.text = text;
    }

    void attachTo(BuildingRule rule) {
        this.rule = rule;
    }

    public Long getId() { return id; }
    public BuildingRule getRule() { return rule; }
    public String getLabel() { return label; }
    public int getPageStart() { return pageStart; }
    public int getPageEnd() { return pageEnd; }
    public String getText() { return text; }
}

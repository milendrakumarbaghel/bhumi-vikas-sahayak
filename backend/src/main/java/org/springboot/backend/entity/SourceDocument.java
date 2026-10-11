package org.springboot.backend.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "source_document")
public class SourceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 8)
    private String language;

    @Column(nullable = false)
    private String authority;

    @Column(name = "version_date", nullable = false)
    private LocalDate versionDate;

    @Column(name = "resource_path", nullable = false)
    private String resourcePath;

    @Column(name = "sha256", length = 64)
    private String sha256;

    protected SourceDocument() {
    }

    public SourceDocument(String title, String language, String authority, LocalDate versionDate, String resourcePath) {
        this.title = title;
        this.language = language;
        this.authority = authority;
        this.versionDate = versionDate;
        this.resourcePath = resourcePath;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getLanguage() { return language; }
    public String getAuthority() { return authority; }
    public LocalDate getVersionDate() { return versionDate; }
    public String getResourcePath() { return resourcePath; }
    public String getSha256() { return sha256; }
}

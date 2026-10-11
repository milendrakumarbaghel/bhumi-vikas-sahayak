package org.springboot.backend.dto;

public record CitationResponse(
        String ruleNumber,
        String title,
        String passageLabel,
        int pageStart,
        int pageEnd,
        String sourceUrl,
        String excerpt,
        boolean validated) {
}

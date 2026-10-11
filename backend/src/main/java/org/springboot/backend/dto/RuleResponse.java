package org.springboot.backend.dto;

import java.util.List;

public record RuleResponse(
        Long id,
        String ruleNumber,
        String title,
        String part,
        int pageStart,
        int pageEnd,
        String text,
        List<Passage> passages) {

    public record Passage(
            Long id,
            String label,
            int pageStart,
            int pageEnd,
            String text) {
    }
}

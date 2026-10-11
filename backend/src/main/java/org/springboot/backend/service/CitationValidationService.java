package org.springboot.backend.service;

import java.util.Arrays;
import java.util.Comparator;

import org.springboot.backend.entity.RulePassage;
import org.springframework.stereotype.Service;

@Service
public class CitationValidationService {

    public boolean isValid(RulePassage passage, String excerpt) {
        if (passage == null || excerpt == null || excerpt.isBlank()) {
            return false;
        }
        return normalize(passage.getText()).contains(normalize(excerpt));
    }

    public String excerpt(RulePassage passage, int maximumLength) {
        String flat = passage.getText().replaceAll("\\s+", " ").strip();
        return flat.length() <= maximumLength ? flat : flat.substring(0, maximumLength);
    }

    public String relevantExcerpt(RulePassage passage, String question, int maximumLength) {
        String flat = passage.getText().replaceAll("\\s+", " ").strip();
        if (flat.length() <= maximumLength) return flat;
        String term = Arrays.stream(question.toLowerCase(java.util.Locale.ROOT)
                        .split("[^\\p{L}\\p{N}]+"))
                .filter(value -> value.length() >= 4)
                .filter(value -> flat.toLowerCase(java.util.Locale.ROOT).contains(value))
                .max(Comparator.comparingInt(String::length))
                .orElse("");
        if (term.isBlank()) return flat.substring(0, maximumLength);
        int match = flat.toLowerCase(java.util.Locale.ROOT).indexOf(term);
        int start = Math.max(0, match - maximumLength / 3);
        int end = Math.min(flat.length(), start + maximumLength);
        return flat.substring(start, end).strip();
    }

    private static String normalize(String value) {
        return value.replaceAll("\\s+", " ").strip();
    }
}

package org.springboot.backend.service;

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

    private static String normalize(String value) {
        return value.replaceAll("\\s+", " ").strip();
    }
}

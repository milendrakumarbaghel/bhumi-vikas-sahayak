package org.springboot.backend.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/** Splits a rule at numbered sub-rule boundaries while keeping each table with its clause. */
@Component
public class RulePassageChunker {

    private static final Pattern SUB_RULE = Pattern.compile("(?m)^\\h*(\\(\\d+[A-Za-z]?\\))\\h+(?=[\\p{L}\\\"\\[])");
    private static final Pattern PAGE_HEADER = Pattern.compile("(?m)^Rule\\s+.+?\\s+(\\d{1,3})\\s*$");

    public List<Passage> chunk(String ruleNumber, int firstPage, int lastPage, String text) {
        List<Boundary> boundaries = new ArrayList<>();
        Matcher matcher = SUB_RULE.matcher(text);
        int lastNumber=0;
        while (matcher.find()) {
            int number=Integer.parseInt(matcher.group(1).replaceAll("[^0-9]",""));
            // Numbering restarts inside explanations and tables. Those are nested content,
            // not independent top-level clauses with duplicate labels.
            if(number<=lastNumber) continue;
            lastNumber=number;
            boundaries.add(new Boundary(matcher.start(), matcher.group(1)));
        }
        if (boundaries.isEmpty()) {
            return List.of(new Passage("Rule " + ruleNumber, firstPage, lastPage, text.strip()));
        }

        List<Passage> result = new ArrayList<>();
        if (!text.substring(0, boundaries.getFirst().offset()).isBlank()) {
            result.add(passage(ruleNumber, "", firstPage, lastPage, text, 0, boundaries.getFirst().offset()));
        }
        for (int index = 0; index < boundaries.size(); index++) {
            Boundary boundary = boundaries.get(index);
            int end = index + 1 < boundaries.size() ? boundaries.get(index + 1).offset() : text.length();
            result.add(passage(ruleNumber, boundary.label(), firstPage, lastPage, text, boundary.offset(), end));
        }
        return List.copyOf(result);
    }

    private static Passage passage(
            String ruleNumber, String suffix, int firstPage, int lastPage, String text, int start, int end) {
        int pageStart = pageAt(text, start, firstPage, lastPage);
        int pageEnd = pageAt(text, Math.max(start, end - 1), pageStart, lastPage);
        String label = suffix.isBlank() ? "Rule " + ruleNumber : "Rule " + ruleNumber + " " + suffix;
        return new Passage(label, pageStart, Math.max(pageStart, pageEnd), text.substring(start, end).strip());
    }

    private static int pageAt(String text, int offset, int fallback, int lastPage) {
        Matcher headers = PAGE_HEADER.matcher(text.substring(0, Math.min(offset, text.length())));
        int page = fallback;
        while (headers.find()) {
            page = Integer.parseInt(headers.group(1));
        }
        return Math.max(fallback, Math.min(page, lastPage));
    }

    private record Boundary(int offset, String label) {
    }

    public record Passage(String label, int pageStart, int pageEnd, String text) {
    }
}

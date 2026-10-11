package org.springboot.backend.rules;

import java.util.List;

/**
 * One rule of the MP Bhumi Vikas Rules 2012, with PDF noise removed.
 *
 * @param number        rule number as printed, e.g. "56" or "26-B"
 * @param insertedRules rules such as 17-a that the PDF prints inside this rule's text
 */
public record Rule(
        String number,
        String title,
        String part,
        int pageStart,
        int pageEnd,
        String text,
        List<InsertedRule> insertedRules,
        RuleTextCleaner.CleanedText cleaned) {

    public int pageAt(int offset) {
        return cleaned.pageAt(offset);
    }
}

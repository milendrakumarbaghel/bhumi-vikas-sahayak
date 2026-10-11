package org.springboot.backend.rules;

/**
 * A searchable piece of a rule: the whole rule, one sub-rule, or one inserted rule.
 * Tables are never split across chunks.
 *
 * @param subRule e.g. "(2)", "(1)-(3)" or "17-a"; empty when the chunk is the whole rule
 */
public record Chunk(
        String id,
        String rule,
        String subRule,
        String title,
        String part,
        int pageStart,
        int pageEnd,
        boolean containsTable,
        String text) {

    /** "Rule 17 (17-a)" or "Rule 56 (2)" or "Rule 21". */
    public String label() {
        return subRule.isEmpty() ? "Rule " + rule : "Rule " + rule + " " + (subRule.startsWith("(") ? subRule : "(" + subRule + ")");
    }
}

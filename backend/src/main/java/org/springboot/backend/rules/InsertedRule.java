package org.springboot.backend.rules;

/**
 * A rule inserted by amendment that sits inside the previous rule's text,
 * e.g. 17-a inside rule 17.
 *
 * @param id          number as printed in the PDF, e.g. "17-a", "25(A)"
 * @param startOffset where it starts in the parent rule's cleaned text
 */
public record InsertedRule(String id, String parentRule, String title, int startOffset, int page) {
}

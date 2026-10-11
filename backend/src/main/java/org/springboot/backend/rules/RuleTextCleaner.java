package org.springboot.backend.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Removes PDF noise from a rule's text and records which PDF page each part of
 * the cleaned text came from.
 *
 * <ul>
 *   <li>Running headers such as {@code "Rule 56   M.P. Bhumi Vikas Rules, 2012   78"} are
 *       dropped. The number at the end is the PDF page that starts there, so we use it to
 *       track pages.</li>
 *   <li>Footnote markers ({@code "1["} inline, or a lone {@code "1"} line before a
 *       {@code "["}) are removed; the bracket itself stays, so amended text is still visible.</li>
 *   <li>Indentation is kept, because flattened tables rely on spacing.</li>
 * </ul>
 */
public final class RuleTextCleaner {

    private static final Pattern RUNNING_HEADER =
            Pattern.compile("^\\s*Rule\\s+\\S.*M\\.P\\. Bhumi Vikas Rules.*$");
    private static final Pattern HEADER_PAGE = Pattern.compile("(\\d+)\\s*$");
    private static final Pattern LONE_DIGIT = Pattern.compile("^\\s*\\d\\s*$");
    // "up to 1[186 sqm]" -> "up to [186 sqm]"
    private static final Pattern INLINE_FOOTNOTE_MARK = Pattern.compile("(?<![\\w\\]])\\d(?=\\[)");

    private RuleTextCleaner() {
    }

    public static CleanedText clean(String rawText, int pageStart) {
        String[] lines = rawText.replace("\r", "").split("\n", -1);
        StringBuilder out = new StringBuilder();
        List<int[]> pageMarks = new ArrayList<>();
        pageMarks.add(new int[]{0, pageStart});
        int blankRun = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].stripTrailing();

            if (RUNNING_HEADER.matcher(line).matches()) {
                Matcher page = HEADER_PAGE.matcher(line);
                if (page.find()) {
                    pageMarks.add(new int[]{out.length(), Integer.parseInt(page.group(1))});
                }
                continue;
            }
            if (LONE_DIGIT.matcher(line).matches() && nextNonBlankStartsWithBracket(lines, i)) {
                continue;
            }

            line = INLINE_FOOTNOTE_MARK.matcher(line).replaceAll("");
            if (line.isBlank()) {
                blankRun++;
                if (blankRun > 1) {
                    continue; // keep at most one blank line in a row
                }
                line = "";
            } else {
                blankRun = 0;
            }
            out.append(line).append('\n');
        }

        String full = out.toString();
        String text = full.strip();
        int leading = full.length() - full.stripLeading().length();
        int[] offsets = new int[pageMarks.size()];
        int[] pages = new int[pageMarks.size()];
        for (int i = 0; i < pageMarks.size(); i++) {
            offsets[i] = Math.max(0, pageMarks.get(i)[0] - leading);
            pages[i] = pageMarks.get(i)[1];
        }
        return new CleanedText(text, offsets, pages);
    }

    private static boolean nextNonBlankStartsWithBracket(String[] lines, int index) {
        for (int j = index + 1; j < lines.length; j++) {
            String next = lines[j].strip();
            if (!next.isEmpty()) {
                return next.startsWith("[") || next.startsWith("\"[");
            }
        }
        return false;
    }

    /** Cleaned text plus a sorted list of (offset, PDF page) marks. */
    public record CleanedText(String text, int[] offsets, int[] pages) {

        public int pageAt(int offset) {
            int page = pages[0];
            for (int i = 0; i < offsets.length && offsets[i] <= offset; i++) {
                page = pages[i];
            }
            return page;
        }
    }
}

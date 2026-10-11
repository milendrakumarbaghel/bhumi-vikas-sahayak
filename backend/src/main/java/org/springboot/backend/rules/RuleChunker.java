package org.springboot.backend.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits a rule into search chunks.
 *
 * <ol>
 *   <li>Each inserted rule (e.g. 17-a) becomes its own segment.</li>
 *   <li>Segments longer than {@link #MAX_CHUNK} are split at numbered sub-rules "(1)", "(2)" ...</li>
 *   <li>Pieces still too long are split at clauses "(a)", "(b)" ...</li>
 *   <li>A split point inside a table is never used, so a table stays in one chunk.</li>
 *   <li>Very small pieces are merged with the next one.</li>
 * </ol>
 */
public final class RuleChunker {

    static final int MAX_CHUNK = 2000;
    static final int MIN_CHUNK = 400;

    // "(2) Rear Open Space" but not a table column header like "(1)   (2)   (3)"
    private static final Pattern SUB_RULE = Pattern.compile("(?m)^[ \\t\\[\"]{0,12}\\((\\d{1,2})\\)\\s+(?=[^\\s(])");
    private static final Pattern CLAUSE = Pattern.compile("(?m)^[ \\t\\[\"]{0,24}\\(([a-z])\\)\\s+(?=[^\\s(])");
    // A table line has at least two gaps of 3+ spaces between words, e.g. "1.   Up to 9.0 meters      3.0 meters"
    private static final Pattern TABLE_LINE = Pattern.compile("\\S {3,}\\S.* {3,}\\S");

    private RuleChunker() {
    }

    public static List<Chunk> chunk(Rule rule) {
        String text = rule.text();
        List<int[]> tables = tableRegions(text);

        // 1. Segments: the main rule body, then one per inserted rule.
        List<Segment> segments = new ArrayList<>();
        int segStart = 0;
        String segLabel = "";
        for (InsertedRule inserted : rule.insertedRules()) {
            segments.add(new Segment(segStart, inserted.startOffset(), segLabel));
            segStart = inserted.startOffset();
            segLabel = inserted.id();
        }
        segments.add(new Segment(segStart, text.length(), segLabel));

        // 2-3. Split long segments at sub-rules, then clauses.
        List<Segment> pieces = new ArrayList<>();
        for (Segment segment : segments) {
            if (segment.end - segment.start <= MAX_CHUNK) {
                pieces.add(segment);
                continue;
            }
            for (Segment bySubRule : split(text, segment, SUB_RULE, tables, true)) {
                if (bySubRule.end - bySubRule.start <= MAX_CHUNK) {
                    pieces.add(bySubRule);
                } else {
                    pieces.addAll(split(text, bySubRule, CLAUSE, tables, false));
                }
            }
        }

        // 5. Merge small pieces into the following piece of the same segment.
        List<Segment> merged = new ArrayList<>();
        Segment pending = null;
        for (Segment piece : pieces) {
            if (pending != null && sameSegment(pending, piece)) {
                piece = new Segment(pending.start, piece.end, joinLabels(pending.label, piece.label));
            } else if (pending != null) {
                merged.add(pending);
            }
            pending = piece;
            if (piece.end - piece.start >= MIN_CHUNK) {
                merged.add(piece);
                pending = null;
            }
        }
        if (pending != null) {
            merged.add(pending);
        }

        List<Chunk> chunks = new ArrayList<>();
        for (Segment s : merged) {
            String body = text.substring(s.start, s.end).strip();
            if (body.isEmpty()) {
                continue;
            }
            boolean hasTable = tables.stream().anyMatch(t -> t[0] < s.end && t[1] > s.start);
            String id = rule.number() + "#" + chunks.size();
            chunks.add(new Chunk(id, rule.number(), s.label, rule.title(), rule.part(),
                    rule.pageAt(s.start), rule.pageAt(Math.max(s.start, s.end - 1)), hasTable, body));
        }
        return chunks;
    }

    /**
     * Splits a segment at markers that are not inside a table. Markers must follow in
     * sequence ((1), (2), (3) or (a), (b), (c)); a marker that restarts or jumps is a list
     * nested inside a clause, not a new sub-rule, so it is not a split point.
     */
    private static List<Segment> split(String text, Segment seg, Pattern marker, List<int[]> tables, boolean numbered) {
        List<Segment> out = new ArrayList<>();
        Matcher m = marker.matcher(text).region(seg.start, seg.end);
        int start = seg.start;
        String label = seg.label;
        int last = 0;
        while (m.find()) {
            String found = m.group(1);
            int n = numbered ? Integer.parseInt(found) : found.charAt(0) - 'a' + 1;
            if (n <= last || n > last + 3 || insideTable(m.start(), tables)) {
                continue;
            }
            last = n;
            String nextLabel = subLabel(seg.label, found, numbered);
            if (m.start() == seg.start) {
                label = nextLabel;
                continue;
            }
            out.add(new Segment(start, m.start(), label));
            start = m.start();
            label = nextLabel;
        }
        out.add(new Segment(start, seg.end, label));
        return out;
    }

    /** "" + 3 -> "(3)"; "(3)" + b -> "(3)(b)"; an inserted rule like "17-a" keeps its own label. */
    private static String subLabel(String parentLabel, String marker, boolean numbered) {
        if (!parentLabel.isEmpty() && !parentLabel.startsWith("(")) {
            return parentLabel;
        }
        return numbered ? "(" + marker + ")" : parentLabel + "(" + marker + ")";
    }

    private static boolean insideTable(int offset, List<int[]> tables) {
        return tables.stream().anyMatch(t -> offset > t[0] && offset < t[1]);
    }

    /**
     * Finds runs of table-looking lines. Lines between two table lines (wrapped cells, blank
     * lines) count as part of the table if the gap is at most 4 lines and none of them starts
     * a new sub-rule or clause.
     */
    static List<int[]> tableRegions(String text) {
        List<int[]> regions = new ArrayList<>();
        String[] lines = text.split("\n", -1);
        int offset = 0;
        int regionStart = -1;
        int regionEnd = -1;
        int linesSinceTable = 0;
        int tableLines = 0;
        for (String line : lines) {
            int lineEnd = offset + line.length();
            if (TABLE_LINE.matcher(line).find()) {
                if (regionStart < 0 || linesSinceTable > 4) {
                    if (regionStart >= 0 && tableLines >= 2) {
                        regions.add(new int[]{regionStart, regionEnd});
                    }
                    regionStart = offset;
                    tableLines = 0;
                }
                regionEnd = lineEnd;
                linesSinceTable = 0;
                tableLines++;
            } else if (SUB_RULE.matcher(line).find() || CLAUSE.matcher(line).find()) {
                // A new "(b)" or "(3)" ends the table even if another table follows soon after.
                if (regionStart >= 0 && tableLines >= 2) {
                    regions.add(new int[]{regionStart, regionEnd});
                }
                regionStart = -1;
                tableLines = 0;
            } else {
                linesSinceTable++;
            }
            offset = lineEnd + 1;
        }
        if (regionStart >= 0 && tableLines >= 2) {
            regions.add(new int[]{regionStart, regionEnd});
        }
        return regions;
    }

    private static boolean sameSegment(Segment a, Segment b) {
        // Inserted-rule labels like "17-a" must not merge with the parent's sub-rules.
        boolean aInserted = !a.label.isEmpty() && !a.label.startsWith("(");
        boolean bInserted = !b.label.isEmpty() && !b.label.startsWith("(");
        return aInserted == bInserted && (!aInserted || a.label.equals(b.label));
    }

    private static String joinLabels(String first, String second) {
        if (first.isEmpty()) {
            return second;
        }
        if (second.isEmpty() || second.startsWith(first) || !first.startsWith("(")) {
            return first;
        }
        // "(1)" + "(2)" -> "(1)-(2)";  "(3)(a)-(b)" + "(3)(c)" -> "(3)(a)-(c)"
        // "(4)" + "(6)(a)" -> "(4)-(6)(a)"
        String from = first.contains("-") ? first.substring(0, first.indexOf('-')) : first;
        String fromTop = from.substring(0, from.indexOf(')') + 1);
        String secondTop = second.substring(0, second.indexOf(')') + 1);
        return from + "-" + (fromTop.equals(secondTop) ? second.substring(second.lastIndexOf('(')) : second);
    }

    private record Segment(int start, int end, String label) {
    }
}

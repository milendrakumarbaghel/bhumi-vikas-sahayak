package org.springboot.backend.citation;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springboot.backend.integration.ai.AiAnswerClient;
import org.springboot.backend.rules.Chunk;
import org.springboot.backend.rules.Rule;
import org.springboot.backend.rules.RuleRepository;
import org.springframework.stereotype.Component;

/**
 * Checks an AI-proposed answer against the loaded rule text before anyone sees it.
 *
 * <ol>
 *   <li>Every quote must be an exact substring of its rule's text (after normalising
 *       whitespace, curly quotes and dashes).</li>
 *   <li>Every rule number named in the answer must exist in the loaded rules.</li>
 *   <li>Every other number in the answer must appear in the question, in the text of a
 *       cited rule, or in the backend calculation - so no invented fees or distances.</li>
 *   <li>An "answered" result needs at least one verified citation; "needs_more_info" needs
 *       at least one missing input.</li>
 * </ol>
 */
@Component
public class AnswerVerifier {

    static final int MIN_QUOTE_CHARS = 12;

    // "Rule 56(1)(a)", "rule 26-B", "नियम 21(3)(b)", "Rules 56 and 57"
    private static final Pattern RULE_MENTION = Pattern.compile(
            "(?:\\b[Rr]ules?|नियम)\\s*(\\d+(?:\\s*-\\s*[A-Za-z]\\b|\\s*[A-Za-z]\\b)?)((?:\\s*\\([0-9A-Za-z]+\\))*)");
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:[.,]\\d+)*");

    private final RuleRepository rules;

    public AnswerVerifier(RuleRepository rules) {
        this.rules = rules;
    }

    /**
     * @param chunksById the chunks that were sent to the model, by ID
     * @param extraText  other trusted text whose numbers may appear in the answer
     *                   (the question, the calculation); may be empty
     */
    public Result verify(AiAnswerClient.Proposal proposal, Map<String, Chunk> chunksById, String extraText) {
        List<String> problems = new ArrayList<>();
        List<VerifiedCitation> verified = new ArrayList<>();

        for (AiAnswerClient.CitedQuote cited : proposal.citations()) {
            Chunk chunk = chunksById.get(cited.chunkId());
            if (chunk == null) {
                problems.add("citation points to a rule extract that was not supplied");
                continue;
            }
            Optional<Rule> rule = rules.find(chunk.rule());
            String quote = trimQuote(cited.quote());
            if (rule.isEmpty()) {
                problems.add("Rule " + chunk.rule() + " does not exist");
            } else if (normalize(quote).length() < MIN_QUOTE_CHARS) {
                problems.add("quote from Rule " + chunk.rule() + " is too short to check");
            } else if (!normalize(rule.get().text()).contains(normalize(quote))) {
                problems.add("quote is not in the text of Rule " + chunk.rule());
            } else {
                verified.add(new VerifiedCitation(chunk, quote));
            }
        }

        String answer = proposal.answer();
        for (String number : ruleNumbersMentioned(answer)) {
            if (rules.find(number).isEmpty()) {
                problems.add("answer names Rule " + number + ", which is not in the loaded rules");
            }
        }

        StringBuilder trusted = new StringBuilder(extraText == null ? "" : extraText);
        verified.forEach(v -> rules.find(v.chunk().rule()).ifPresent(r -> trusted.append('\n').append(r.text())));
        Set<String> allowed = numbersIn(trusted.toString());
        for (String number : numbersIn(withoutRuleMentions(answer))) {
            if (!allowed.contains(number)) {
                problems.add("answer states " + number + ", which is not in the cited rules or the question");
            }
        }

        switch (proposal.status()) {
            case "answered" -> {
                if (verified.isEmpty()) {
                    problems.add("answer has no verified citation");
                }
            }
            case "needs_more_info" -> {
                if (proposal.missingInputs().isEmpty()) {
                    problems.add("asks for more information but names no missing input");
                }
            }
            default -> {
                // "not_found" makes no claims, so there is nothing to check.
            }
        }
        return new Result(problems.isEmpty(), List.copyOf(verified), List.copyOf(problems));
    }

    /** Collapses whitespace and unifies quote and dash characters. */
    static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKC)
                .replaceAll("[‘’‚′`]", "'")
                .replaceAll("[“”„″]", "\"")
                .replaceAll("[‐-―−]", "-")
                .replaceAll("\\s+", " ")
                .strip();
    }

    /** Drops surrounding quote marks and trailing sentence punctuation the model may add. */
    static String trimQuote(String quote) {
        return quote.strip()
                .replaceAll("^[\"'“‘]+|[\"'”’]+$", "")
                .replaceAll("[.;,:]+$", "")
                .strip();
    }

    static List<String> ruleNumbersMentioned(String answer) {
        List<String> numbers = new ArrayList<>();
        Matcher m = RULE_MENTION.matcher(answer);
        while (m.find()) {
            numbers.add(m.group(1).replaceAll("\\s+", ""));
        }
        return numbers;
    }

    private static String withoutRuleMentions(String answer) {
        return RULE_MENTION.matcher(answer).replaceAll(" ");
    }

    /** Numbers as canonical strings: "1,250" -> "1250", "3.0" -> "3", "3.60" -> "3.6". */
    static Set<String> numbersIn(String text) {
        Set<String> numbers = new LinkedHashSet<>();
        Matcher m = NUMBER.matcher(asciiDigits(text));
        while (m.find()) {
            String raw = m.group();
            // Indian grouping "1,25,000" and plain "1,250" are thousands separators; a lone
            // comma followed by 1-2 digits elsewhere is just a list separator.
            String number = raw.matches("\\d{1,3}(,\\d{2,3})+(\\.\\d+)?") ? raw.replace(",", "") : raw.split(",")[0];
            try {
                numbers.add(new java.math.BigDecimal(number).stripTrailingZeros().toPlainString());
            } catch (NumberFormatException ignored) {
                // not a number after all
            }
        }
        return numbers;
    }

    private static String asciiDigits(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            sb.append(c >= '०' && c <= '९' ? (char) ('0' + (c - '०')) : c);
        }
        return sb.toString();
    }

    public record VerifiedCitation(Chunk chunk, String quote) {
    }

    /** @param problems plain-English reasons; never contain the question text */
    public record Result(boolean passed, List<VerifiedCitation> citations, List<String> problems) {
    }
}

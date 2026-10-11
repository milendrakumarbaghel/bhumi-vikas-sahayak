package org.springboot.backend.service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springboot.backend.entity.RulePassage;
import org.springboot.backend.integration.ai.GroundedAiClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/** Fails closed when an AI answer contains an unknown citation, rule or number. */
@Service
public class GeneratedAnswerValidationService {

    private static final Pattern RULE = Pattern.compile(
            "(?:\\b[Rr]ules?|नियम)\\s*(\\d+(?:\\s*-\\s*[A-Za-z])?)");
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:[.,]\\d+)*");
    private final JsonMapper json;

    public GeneratedAnswerValidationService(JsonMapper json) {
        this.json = json;
    }

    public Validation validate(
            GroundedAiClient.Proposal proposal,
            List<RulePassage> supplied,
            String question,
            Map<String, Object> calculation) {
        List<String> problems = new ArrayList<>();
        if (!Set.of("answered", "not_found", "needs_more_info").contains(proposal.status())) {
            problems.add("unknown answer status");
        }
        Map<String, RulePassage> byId = new LinkedHashMap<>();
        supplied.forEach(value -> byId.put(GroundedAiClient.chunkId(value), value));
        List<VerifiedCitation> verified = new ArrayList<>();
        for (GroundedAiClient.CitedQuote citation : proposal.citations()) {
            RulePassage passage = byId.get(citation.chunkId());
            if (passage == null) {
                problems.add("citation was not supplied to the AI service");
            } else if (normalize(citation.quote()).length() < 12) {
                problems.add("citation quote is too short");
            } else if (!normalize(passage.getText()).contains(normalize(citation.quote()))) {
                problems.add("citation quote does not occur in the stored passage");
            } else {
                verified.add(new VerifiedCitation(passage, citation.quote().strip()));
            }
        }
        if ("answered".equals(proposal.status()) && verified.isEmpty()) {
            problems.add("answered response has no verified citation");
        }
        if ("not_found".equals(proposal.status()) && !verified.isEmpty()) {
            problems.add("not-found response must not include citations");
        }
        if ("needs_more_info".equals(proposal.status()) && proposal.missingInputs().isEmpty()) {
            problems.add("missing-information response names no missing input");
        }
        if (!"not_found".equals(proposal.status())) {
            if (proposal.answer().isBlank() || proposal.claims().isEmpty() || proposal.claims().size()>12) {
                problems.add("answer has no complete sentence-level claims");
            }
            String reconstructed=proposal.claims().stream().map(GroundedAiClient.Claim::text)
                    .collect(java.util.stream.Collectors.joining(" "));
            if (!normalize(reconstructed).equals(normalize(proposal.answer()))) {
                problems.add("claims do not cover the entire answer in order");
            }
            Set<String> verifiedIds=new LinkedHashSet<>();
            verified.forEach(value -> verifiedIds.add(GroundedAiClient.chunkId(value.passage())));
            for (var claim:proposal.claims()) {
                if(claim.text().isBlank() || claim.chunkIds().isEmpty() || !verifiedIds.containsAll(claim.chunkIds()))
                    problems.add("claim has no verified evidence");
                if(claim.text().split("(?<!\\d)[.!?।]+(?!\\d)").length>1)
                    problems.add("claim contains more than one sentence");
            }
        }

        Set<String> citedRules = new LinkedHashSet<>();
        verified.forEach(value -> citedRules.add(canonicalRule(value.passage().getRule().getRuleNumber())));
        Matcher mentioned = RULE.matcher(proposal.answer());
        while (mentioned.find()) {
            if (!citedRules.contains(canonicalRule(mentioned.group(1)))) {
                problems.add("answer names an uncited rule");
            }
        }

        StringBuilder trusted = new StringBuilder(question).append(" 2012 6 2025");
        verified.forEach(value -> trusted.append(' ').append(value.quote()));
        trusted.append(' ').append(json.writeValueAsString(calculation));
        Set<String> allowedNumbers = numbers(trusted.toString());
        String withoutRules = RULE.matcher(proposal.answer()).replaceAll(" ");
        for (String number : numbers(withoutRules)) {
            if (!allowedNumbers.contains(number)) problems.add("answer contains an ungrounded number: " + number);
        }
        return new Validation(problems.isEmpty(), List.copyOf(verified), List.copyOf(problems));
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKC)
                .replaceAll("[‐-―−]", "-").replaceAll("\\s+", " ").strip();
    }

    private static String canonicalRule(String value) {
        return value.toUpperCase(java.util.Locale.ROOT).replaceAll("[\\s-]", "");
    }

    private static Set<String> numbers(String text) {
        Set<String> result = new LinkedHashSet<>();
        Matcher matcher = NUMBER.matcher(asciiDigits(text));
        while (matcher.find()) {
            String raw = matcher.group().replace(",", "");
            try {
                result.add(new BigDecimal(raw).stripTrailingZeros().toPlainString());
            } catch (NumberFormatException ignored) {
                // Ignore malformed numeric-looking text.
            }
        }
        return result;
    }

    private static String asciiDigits(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char value : text.toCharArray()) {
            result.append(value >= '०' && value <= '९' ? (char) ('0' + value - '०') : value);
        }
        return result.toString();
    }

    public record VerifiedCitation(RulePassage passage, String quote) {
    }

    public record Validation(boolean passed, List<VerifiedCitation> citations, List<String> problems) {
    }
}

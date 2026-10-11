package org.springboot.backend.retrieval;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springboot.backend.rules.Chunk;
import org.springboot.backend.rules.InsertedRule;
import org.springboot.backend.rules.Rule;
import org.springboot.backend.rules.RuleRepository;
import org.springframework.stereotype.Component;

/**
 * Finds the rule chunks to send to the answer step:
 * <ol>
 *   <li>rules the question names explicitly ("rule 21"),</li>
 *   <li>rules pinned for each detected topic (e.g. fees always brings in rule 21),</li>
 *   <li>then the best BM25 matches, until {@link #MAX_CHUNKS} chunks are chosen.</li>
 * </ol>
 */
@Component
public class Retriever {

    public static final int MAX_CHUNKS = 8;
    private static final int MAX_PINNED = 5;

    private final QueryNormalizer normalizer;
    private final RuleSearch search;
    private final RuleRepository rules;

    public Retriever(QueryNormalizer normalizer, RuleSearch search, RuleRepository rules) {
        this.normalizer = normalizer;
        this.search = search;
        this.rules = rules;
    }

    public Result retrieve(String question) {
        QueryIntent intent = normalizer.normalize(question);
        return new Result(intent, retrieve(intent));
    }

    public List<RetrievedChunk> retrieve(QueryIntent intent) {
        Map<String, RetrievedChunk> chosen = new LinkedHashMap<>();
        String terms = intent.searchTerms() + " " + intent.englishQuestion();

        // Explicit references first, then each topic's top rule, then each topic's second rule...
        List<String> pinned = new ArrayList<>(intent.ruleRefs());
        for (int rank = 0; rank < 3; rank++) {
            for (Topic topic : intent.topics()) {
                if (rank < topic.pinnedRules.size()) {
                    pinned.add(topic.pinnedRules.get(rank));
                }
            }
        }
        for (String ref : pinned) {
            if (chosen.size() >= MAX_PINNED) {
                break;
            }
            bestChunkFor(ref, terms).ifPresent(hit ->
                    chosen.putIfAbsent(hit.chunk().id(), new RetrievedChunk(hit.chunk(), hit.score(), "topic")));
        }

        for (RuleSearch.Hit hit : search.search(terms, MAX_CHUNKS * 2)) {
            if (chosen.size() >= MAX_CHUNKS) {
                break;
            }
            chosen.putIfAbsent(hit.chunk().id(), new RetrievedChunk(hit.chunk(), hit.score(), "search"));
        }
        return List.copyOf(chosen.values());
    }

    /**
     * The best chunk for a rule reference such as "21", "12-A" or "2(5)(c)". A reference that
     * names a sub-rule returns the chunk whose label starts with that sub-rule.
     */
    private Optional<RuleSearch.Hit> bestChunkFor(String ref, String terms) {
        String parent;
        String subRule;
        Optional<InsertedRule> inserted = rules.findInserted(ref);
        if (inserted.isPresent()) {
            parent = inserted.get().parentRule();
            subRule = inserted.get().id();
        } else {
            String ruleNumber = ref.replaceFirst("\\(.*$", "");
            parent = rules.find(ruleNumber).map(Rule::number).orElse(null);
            subRule = ref.substring(ruleNumber.length());
        }
        if (parent == null) {
            return Optional.empty();
        }
        // Chunks of the rule, best match first (all of them, score 0, if nothing matches).
        for (RuleSearch.Hit hit : search.searchWithinRule(parent, terms, 50)) {
            String label = hit.chunk().subRule();
            boolean matches = inserted.isPresent() ? label.equals(subRule) : label.startsWith(subRule);
            if (matches) {
                return Optional.of(hit);
            }
        }
        return Optional.empty();
    }

    public record Result(QueryIntent intent, List<RetrievedChunk> chunks) {
    }

    /** @param reason "topic" when pinned by topic or explicit rule reference, "search" for BM25 */
    public record RetrievedChunk(Chunk chunk, float score, String reason) {
    }
}

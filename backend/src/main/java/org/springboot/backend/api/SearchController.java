package org.springboot.backend.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springboot.backend.application.RuleQueryService;
import org.springboot.backend.retrieval.QueryIntent;
import org.springboot.backend.retrieval.Retriever;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public rule-search endpoint. AI-service details are never exposed to the browser. */
@RestController
@RequestMapping("/api")
public class SearchController {

    private final RuleQueryService queries;

    public SearchController(RuleQueryService queries) {
        this.queries = queries;
    }

    @PostMapping("/search")
    public SearchResponse search(@Valid @RequestBody QuestionRequest request) {
        Retriever.Result result = queries.search(request.question());
        List<Match> matches = result.chunks().stream()
                .map(item -> new Match(
                        item.chunk().label(),
                        item.chunk().title(),
                        item.chunk().pageStart(),
                        item.chunk().pageEnd(),
                        item.reason(),
                        Math.round(item.score() * 100) / 100.0,
                        snippet(item.chunk().text())))
                .toList();
        return new SearchResponse(result.intent(), matches);
    }

    private static String snippet(String text) {
        String flat = text.replaceAll("\\s+", " ").strip();
        return flat.length() <= 200 ? flat : flat.substring(0, 200) + "...";
    }

    public record QuestionRequest(@NotBlank @Size(max = 600) String question) {
    }

    public record SearchResponse(QueryIntent intent, List<Match> matches) {
    }

    public record Match(
            String rule,
            String title,
            int pageStart,
            int pageEnd,
            String reason,
            double score,
            String snippet) {
    }
}

package org.springboot.backend.web;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springboot.backend.retrieval.QueryIntent;
import org.springboot.backend.retrieval.Retriever;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Shows what the retriever finds for a question. Used to check and tune retrieval. */
@RestController
@RequestMapping("/api")
public class SearchController {

    private final Retriever retriever;

    public SearchController(Retriever retriever) {
        this.retriever = retriever;
    }

    @PostMapping("/search")
    public SearchResponse search(@Valid @RequestBody QuestionRequest request) {
        Retriever.Result result = retriever.retrieve(request.question());
        List<Match> matches = result.chunks().stream()
                .map(r -> new Match(r.chunk().label(), r.chunk().title(), r.chunk().pageStart(), r.chunk().pageEnd(),
                        r.reason(), Math.round(r.score() * 100) / 100.0, snippet(r.chunk().text())))
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

    public record Match(String rule, String title, int pageStart, int pageEnd, String reason, double score, String snippet) {
    }
}

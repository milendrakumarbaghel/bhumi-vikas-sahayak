package org.springboot.backend.api;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springboot.backend.application.AnswerService;
import org.springboot.backend.integration.ai.AiServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The clerk's main endpoint: a question in, a short cited answer out. */
@RestController
@RequestMapping("/api")
public class AskController {

    private static final Logger log = LoggerFactory.getLogger(AskController.class);

    private final AnswerService answers;

    public AskController(AnswerService answers) {
        this.answers = answers;
    }

    @PostMapping("/ask")
    public AnswerService.AskResult ask(@Valid @RequestBody AskRequest request) {
        return answers.ask(request.question().strip(), request.language() == null ? "auto" : request.language());
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<Map<String, String>> aiUnavailable(AiServiceException e) {
        log.warn("Answer generation failed: {}", e.getMessage()); // never log the question
        return ResponseEntity.status(503).body(Map.of(
                "error", "AI_SERVICE_UNAVAILABLE",
                "message", "The answer service is not available right now. You can still search the rules."));
    }

    /** @param language "auto" (default), "en" or "hi" */
    public record AskRequest(
            @NotBlank @Size(min = 3, max = 600) String question,
            @Pattern(regexp = "auto|en|hi") String language) {
    }
}

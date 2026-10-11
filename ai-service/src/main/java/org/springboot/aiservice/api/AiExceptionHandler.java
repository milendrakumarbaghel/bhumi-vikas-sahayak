package org.springboot.aiservice.api;

import java.util.Map;

import org.springboot.aiservice.llm.LlmException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AiExceptionHandler {

    @ExceptionHandler(LlmException.class)
    ResponseEntity<Map<String, String>> handleLlmFailure(LlmException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "code", "AI_PROVIDER_UNAVAILABLE",
                        "message", "The AI provider is unavailable or returned an invalid response"));
    }
}

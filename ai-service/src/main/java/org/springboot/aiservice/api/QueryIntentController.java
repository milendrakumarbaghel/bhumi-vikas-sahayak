package org.springboot.aiservice.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springboot.aiservice.application.QueryIntentService;
import org.springboot.aiservice.application.QueryIntentService.Analysis;
import org.springboot.aiservice.llm.LlmClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/v1")
public class QueryIntentController {

    private final QueryIntentService service;
    private final LlmClient llm;
    private final String internalToken;

    public QueryIntentController(
            QueryIntentService service,
            LlmClient llm,
            @Value("${bvs.internal-token:}") String internalToken) {
        this.service = service;
        this.llm = llm;
        this.internalToken = internalToken == null ? "" : internalToken;
    }

    @PostMapping("/query-intents")
    public Analysis analyse(
            @RequestHeader(value = "x-internal-token", required = false) String token,
            @Valid @RequestBody QueryRequest request) {
        authorize(token);
        if (!llm.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "LLM provider is not configured");
        }
        return service.analyse(request.question());
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "providerConfigured", llm.isConfigured());
    }

    private void authorize(String suppliedToken) {
        if (internalToken.isBlank()) {
            return;
        }
        byte[] expected = internalToken.getBytes(StandardCharsets.UTF_8);
        byte[] actual = (suppliedToken == null ? "" : suppliedToken).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal token");
        }
    }

    public record QueryRequest(@NotBlank @Size(max = 600) String question) {
    }
}

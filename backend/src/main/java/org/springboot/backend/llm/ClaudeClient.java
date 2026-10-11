package org.springboot.backend.llm;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Calls the Anthropic Messages API over plain HTTP. The key comes from ANTHROPIC_API_KEY and
 * the model from ANTHROPIC_MODEL; neither is ever logged.
 */
@Component
public class ClaudeClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(ClaudeClient.class);
    private static final URI MESSAGES_URL = URI.create("https://api.anthropic.com/v1/messages");

    // Models that accept server-side refusal fallbacks ("fallbacks": "default").
    private static final Set<String> FALLBACK_MODELS =
            Set.of("claude-opus-5-5", "claude-opus-5", "claude-fable-5-1", "claude-sonnet-5-5");
    private static final String FALLBACK_BETA = "server-side-fallback-2026-07-01";

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final JsonMapper json;
    private final String apiKey;
    private final String model;

    public ClaudeClient(JsonMapper json,
                        @Value("${anthropic.api-key:}") String apiKey,
                        @Value("${anthropic.model:claude-opus-5-5}") String model) {
        this.json = json;
        this.apiKey = apiKey == null ? "" : apiKey.strip();
        this.model = model == null || model.isBlank() ? "claude-opus-5-5" : model.strip();
    }

    @Override
    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }

    @Override
    public String modelName() {
        return model;
    }

    @Override
    public String completeJson(String systemPrompt, String userMessage, Map<String, Object> jsonSchema, Effort effort) {
        if (!isConfigured()) {
            throw new LlmException("ANTHROPIC_API_KEY is not set");
        }
        boolean fallbacks = FALLBACK_MODELS.contains(model);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", 16000);
        body.put("system", List.of(Map.of(
                "type", "text",
                "text", systemPrompt,
                "cache_control", Map.of("type", "ephemeral"))));
        body.put("messages", List.of(Map.of("role", "user", "content", userMessage)));
        body.put("output_config", Map.of(
                "effort", effort.name(),
                "format", Map.of("type", "json_schema", "schema", jsonSchema)));
        if (fallbacks) {
            body.put("fallbacks", "default");
        }

        HttpRequest.Builder request = HttpRequest.newBuilder(MESSAGES_URL)
                .timeout(Duration.ofSeconds(90))
                .header("content-type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        if (fallbacks) {
            request.header("anthropic-beta", FALLBACK_BETA);
        }

        HttpResponse<String> response = sendWithOneRetry(request.build());
        JsonNode root;
        try {
            root = json.readTree(response.body());
        } catch (RuntimeException e) {
            throw new LlmException("Claude API returned HTTP " + response.statusCode() + " with a non-JSON body", e);
        }
        if (response.statusCode() != 200) {
            String error = root.path("error").path("message").asString("unknown error");
            throw new LlmException("Claude API returned HTTP " + response.statusCode() + ": " + error);
        }

        String stopReason = root.path("stop_reason").asString("");
        if ("refusal".equals(stopReason)) {
            throw new LlmException("Claude declined the request");
        }
        if ("max_tokens".equals(stopReason)) {
            throw new LlmException("Claude's reply was cut off (max_tokens)");
        }

        // The reply may also hold thinking or fallback blocks; only text blocks carry the JSON.
        StringBuilder text = new StringBuilder();
        for (JsonNode block : root.path("content")) {
            if ("text".equals(block.path("type").asString())) {
                text.append(block.path("text").asString());
            }
        }
        if (text.isEmpty()) {
            throw new LlmException("Claude returned no text");
        }
        return text.toString();
    }

    /** Retries once on rate limits, overload and network errors. */
    private HttpResponse<String> sendWithOneRetry(HttpRequest request) {
        for (int attempt = 1; ; attempt++) {
            try {
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                int code = response.statusCode();
                boolean retryable = code == 429 || code == 529 || code >= 500;
                if (!retryable || attempt == 2) {
                    return response;
                }
                log.warn("Claude API returned HTTP {}, retrying once", code);
            } catch (IOException e) {
                if (attempt == 2) {
                    throw new LlmException("Could not reach the Claude API: " + e.getMessage(), e);
                }
                log.warn("Network error calling Claude API, retrying once: {}", e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LlmException("Interrupted while calling the Claude API", e);
            }
            sleepQuietly(1500);
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

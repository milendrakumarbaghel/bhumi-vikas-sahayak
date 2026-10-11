package org.springboot.aiservice.llm;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Anthropic adapter. Provider credentials and prompts never enter the main backend. */
@Component
public class AnthropicClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicClient.class);
    private static final URI MESSAGES_URL = URI.create("https://api.anthropic.com/v1/messages");

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final JsonMapper json;
    private final String apiKey;
    private final String model;

    public AnthropicClient(
            JsonMapper json,
            @Value("${anthropic.api-key:}") String apiKey,
            @Value("${anthropic.model:}") String model) {
        this.json = json;
        this.apiKey = apiKey == null ? "" : apiKey.strip();
        this.model = model == null ? "" : model.strip();
    }

    @Override
    public boolean isConfigured() {
        return !apiKey.isEmpty() && !model.isEmpty();
    }

    @Override
    public String modelName() {
        return model;
    }

    @Override
    public String completeJson(
            String systemPrompt,
            String userMessage,
            Map<String, Object> jsonSchema,
            Effort effort) {
        if (!isConfigured()) {
            throw new LlmException("Anthropic provider is not configured");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", 4096);
        body.put("system", systemPrompt);
        body.put("messages", List.of(Map.of("role", "user", "content", userMessage)));
        body.put("output_config", Map.of(
                "effort", effort.name(),
                "format", Map.of("type", "json_schema", "schema", jsonSchema)));

        HttpRequest request = HttpRequest.newBuilder(MESSAGES_URL)
                .timeout(Duration.ofSeconds(90))
                .header("content-type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = sendWithOneRetry(request);
        JsonNode root;
        try {
            root = json.readTree(response.body());
        } catch (RuntimeException e) {
            throw new LlmException("Provider returned a non-JSON response", e);
        }
        if (response.statusCode() != 200) {
            throw new LlmException("Provider returned HTTP " + response.statusCode());
        }
        String stopReason = root.path("stop_reason").asString("");
        if ("refusal".equals(stopReason) || "max_tokens".equals(stopReason)) {
            throw new LlmException("Provider could not produce a complete structured response");
        }

        StringBuilder result = new StringBuilder();
        for (JsonNode block : root.path("content")) {
            if ("text".equals(block.path("type").asString())) {
                result.append(block.path("text").asString());
            }
        }
        if (result.isEmpty()) {
            throw new LlmException("Provider returned no structured text");
        }
        return result.toString();
    }

    private HttpResponse<String> sendWithOneRetry(HttpRequest request) {
        for (int attempt = 1; ; attempt++) {
            try {
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                boolean retryable = response.statusCode() == 429
                        || response.statusCode() == 529
                        || response.statusCode() >= 500;
                if (!retryable || attempt == 2) {
                    return response;
                }
                log.warn("LLM provider returned HTTP {}; retrying once", response.statusCode());
            } catch (IOException e) {
                if (attempt == 2) {
                    throw new LlmException("Could not reach LLM provider", e);
                }
                log.warn("Network error calling LLM provider; retrying once");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LlmException("Interrupted while calling LLM provider", e);
            }
        }
    }
}

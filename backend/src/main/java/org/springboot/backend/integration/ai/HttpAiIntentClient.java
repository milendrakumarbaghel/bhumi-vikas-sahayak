package org.springboot.backend.integration.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** HTTP adapter for the top-level {@code ai-service/}. Contains no provider or prompt logic. */
@Component
public class HttpAiIntentClient implements AiIntentClient {

    private final HttpClient http;
    private final JsonMapper json;
    private final URI endpoint;
    private final String internalToken;
    private final boolean enabled;
    private final Duration requestTimeout;

    public HttpAiIntentClient(
            JsonMapper json,
            @Value("${bvs.ai-service.base-url:http://localhost:8081}") String baseUrl,
            @Value("${bvs.ai-service.internal-token:}") String internalToken,
            @Value("${bvs.ai-service.enabled:true}") boolean enabled,
            @Value("${bvs.ai-service.timeout:5s}") Duration requestTimeout) {
        this.json = json;
        this.endpoint = URI.create(stripTrailingSlash(baseUrl) + "/internal/v1/query-intents");
        this.internalToken = internalToken == null ? "" : internalToken.strip();
        this.enabled = enabled;
        this.requestTimeout = requestTimeout;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Analysis analyse(String question) {
        if (!enabled) {
            throw new AiServiceException("AI service integration is disabled");
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder(endpoint)
                .timeout(requestTimeout)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        json.writeValueAsString(Map.of("question", question))));
        if (!internalToken.isBlank()) {
            builder.header("x-internal-token", internalToken);
        }

        HttpResponse<String> response;
        try {
            response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new AiServiceException("AI service is unavailable", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiServiceException("Interrupted while calling AI service", e);
        }
        if (response.statusCode() != 200) {
            throw new AiServiceException("AI service returned HTTP " + response.statusCode());
        }

        try {
            JsonNode root = json.readTree(response.body());
            return new Analysis(
                    root.path("language").asString("en"),
                    root.path("englishQuestion").asString(question),
                    root.path("searchTerms").asString(""),
                    strings(root.path("topics")),
                    measure(root.path("plotArea")),
                    measure(root.path("roadWidth")),
                    measure(root.path("buildingHeight")),
                    measure(root.path("builtUpArea")),
                    root.path("buildingUse").asString("unknown"),
                    strings(root.path("ruleRefs")),
                    root.path("model").asString("unknown"));
        } catch (RuntimeException e) {
            throw new AiServiceException("AI service returned an invalid response", e);
        }
    }

    private static List<String> strings(JsonNode node) {
        List<String> values = new ArrayList<>();
        node.forEach(item -> values.add(item.asString()));
        return List.copyOf(values);
    }

    private static Measure measure(JsonNode node) {
        return node.isObject()
                ? new Measure(node.path("value").asDouble(), node.path("unit").asString())
                : null;
    }

    private static String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}

package org.springboot.backend.integration.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** HTTP adapter for {@code POST /internal/v1/answers} on the Python AI service. */
@Component
public class HttpAiAnswerClient implements AiAnswerClient {

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final JsonMapper json;
    private final URI endpoint;
    private final String internalToken;
    private final boolean enabled;
    private final Duration requestTimeout;

    public HttpAiAnswerClient(
            JsonMapper json,
            @Value("${bvs.ai-service.base-url:http://localhost:8081}") String baseUrl,
            @Value("${bvs.ai-service.internal-token:}") String internalToken,
            @Value("${bvs.ai-service.enabled:true}") boolean enabled,
            @Value("${bvs.ai-service.answer-timeout:60s}") Duration requestTimeout) {
        this.json = json;
        this.endpoint = URI.create(baseUrl.replaceAll("/+$", "") + "/internal/v1/answers");
        this.internalToken = internalToken == null ? "" : internalToken.strip();
        this.enabled = enabled;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public Proposal answer(Request request) {
        if (!enabled) {
            throw new AiServiceException("AI service integration is disabled");
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder(endpoint)
                .timeout(requestTimeout)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(request)));
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
            List<CitedQuote> citations = new ArrayList<>();
            for (JsonNode c : root.path("citations")) {
                citations.add(new CitedQuote(c.path("chunkId").asString(), c.path("quote").asString()));
            }
            List<String> missing = new ArrayList<>();
            root.path("missingInputs").forEach(m -> missing.add(m.asString()));
            return new Proposal(
                    root.path("status").asString(),
                    root.path("language").asString("en"),
                    root.path("answer").asString(""),
                    List.copyOf(citations),
                    List.copyOf(missing),
                    root.path("model").asString("unknown"));
        } catch (RuntimeException e) {
            throw new AiServiceException("AI service returned an invalid response", e);
        }
    }
}

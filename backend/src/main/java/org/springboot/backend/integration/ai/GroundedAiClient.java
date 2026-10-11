package org.springboot.backend.integration.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springboot.backend.entity.RulePassage;
import org.springboot.backend.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Internal client for deterministic calculations and grounded answer wording. */
@Service
public class GroundedAiClient {

    private static final Logger log = LoggerFactory.getLogger(GroundedAiClient.class);

    private final HttpClient http;
    private final JsonMapper json;
    private final URI calculationsEndpoint;
    private final URI answersEndpoint;
    private final URI healthEndpoint;
    private final URI verificationEndpoint;
    private final String token;
    private final boolean enabled;
    private final Duration timeout;

    public GroundedAiClient(
            HttpClient http,
            JsonMapper json,
            @Value("${bvs.ai-service.base-url}") String baseUrl,
            @Value("${bvs.ai-service.internal-token:}") String token,
            @Value("${bvs.ai-service.enabled:true}") boolean enabled,
            @Value("${bvs.ai-service.timeout:20s}") Duration timeout) {
        String base = baseUrl.replaceFirst("/+$", "");
        this.http = http;
        this.json = json;
        this.calculationsEndpoint = URI.create(base + "/internal/v1/calculations");
        this.answersEndpoint = URI.create(base + "/internal/v1/answers");
        this.healthEndpoint = URI.create(base + "/internal/v1/health");
        this.verificationEndpoint = URI.create(base + "/internal/v1/claims:verify");
        this.token = token == null ? "" : token.strip();
        this.enabled = enabled;
        this.timeout = timeout;
    }

    public Optional<Map<String, Object>> calculate(AiService.Analysis analysis) {
        if (!enabled) return Optional.empty();
        Map<String, Object> request = facts(analysis);
        List<String> topics=new ArrayList<>(analysis.topics());
        String question=org.springboot.backend.util.QueryLexicon.expand(analysis.englishQuestion());
        if (topics.contains("SETBACK")) {
            List<String> specifics=new ArrayList<>();
            if(question.matches("(?s).*(\\bfront\\b|आगे|सामने).*")) specifics.add("SETBACK_FRONT");
            if(question.matches("(?s).*(\\brear\\b|पीछे).*")) specifics.add("SETBACK_REAR");
            if(question.matches("(?s).*(\\bside\\b|बगल).*")) specifics.add("SETBACK_SIDE");
            if(!specifics.isEmpty()) {topics.remove("SETBACK");topics.addAll(specifics);}
        }
        request.put("topics", topics);
        return post(calculationsEndpoint, request).map(this::asMap);
    }

    public Optional<Proposal> answer(
            String question,
            String language,
            List<RulePassage> passages,
            Map<String, Object> calculation) {
        if (!enabled) return Optional.empty();
        List<Map<String, Object>> chunks = passages.stream().map(passage -> Map.<String, Object>of(
                "id", chunkId(passage),
                "label", passage.getLabel(),
                "title", passage.getRule().getTitle(),
                "pages", pages(passage),
                "text", passage.getText())).toList();
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("question", question);
        request.put("language", language);
        request.put("chunks", chunks);
        if (!calculation.isEmpty()) request.put("calculation", calculation);
        return post(answersEndpoint, request).map(this::proposal);
    }

    public String health() {
        if (!enabled) return "DISABLED";
        try {
            HttpResponse<String> response = http.send(
                    HttpRequest.newBuilder(healthEndpoint).timeout(Duration.ofSeconds(2)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? "UP" : "DOWN";
        } catch (IOException | RuntimeException e) {
            return "DOWN";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "DOWN";
        }
    }

    public boolean verifyClaims(Proposal proposal,Map<String,Object> calculation,String question) {
        if (!enabled || proposal.claims().isEmpty()) return false;
        List<Map<String,Object>> claims=new ArrayList<>();
        for (int i=0;i<proposal.claims().size();i++) {
            Claim claim=proposal.claims().get(i);
            List<String> evidence=proposal.citations().stream()
                    .filter(c -> claim.chunkIds().contains(c.chunkId())).map(CitedQuote::quote).toList();
            claims.add(Map.of("id",i,"text",claim.text(),"evidence",evidence));
        }
        var response=post(verificationEndpoint,Map.of("claims",claims,"calculation",calculation,"question",question));
        if(response.isEmpty()) return false;
        var verdicts=response.get().path("verdicts");
        if(!verdicts.isArray() || verdicts.size()!=claims.size()) return false;
        java.util.Set<Integer> ids=new java.util.HashSet<>();
        for(var verdict:verdicts) {
            int id=verdict.path("id").asInt(-1);
            if(id<0||id>=claims.size()||!ids.add(id)||!"entailed".equals(verdict.path("verdict").asString())) return false;
        }
        return ids.size()==claims.size();
    }

    public static String chunkId(RulePassage passage) {
        return passage.getId() == null
                ? passage.getRule().getRuleNumber() + ":" + passage.getLabel()
                : passage.getId().toString();
    }

    private Optional<JsonNode> post(URI endpoint, Map<String, Object> body) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(endpoint)
                    .timeout(timeout)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
            if (!token.isBlank()) request.header("x-internal-token", token);
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("AI service {} returned HTTP {}", endpoint.getPath(), response.statusCode());
                return Optional.empty();
            }
            return Optional.of(json.readTree(response.body()));
        } catch (IOException | RuntimeException e) {
            log.warn("AI service {} unavailable or invalid: {}", endpoint.getPath(), e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(JsonNode node) {
        return json.readValue(node.toString(), Map.class);
    }

    private Proposal proposal(JsonNode node) {
        List<CitedQuote> citations = new ArrayList<>();
        node.path("citations").forEach(value -> citations.add(new CitedQuote(
                value.path("chunkId").asString(), value.path("quote").asString())));
        List<String> missing = new ArrayList<>();
        node.path("missingInputs").forEach(value -> missing.add(value.asString()));
        Map<String, Object> calculations = node.path("calculations").isObject()
                ? asMap(node.path("calculations")) : Map.of();
        List<Claim> claims = new ArrayList<>();
        node.path("claims").forEach(value -> {
            List<String> ids=new ArrayList<>();value.path("chunkIds").forEach(id -> ids.add(id.asString()));
            claims.add(new Claim(value.path("text").asString(""),List.copyOf(ids)));
        });
        return new Proposal(
                node.path("status").asString("not_found"),
                node.path("language").asString("en"),
                node.path("answer").asString(""),
                List.copyOf(citations), List.copyOf(missing), calculations,
                node.path("model").asString("unknown"),List.copyOf(claims));
    }

    private static Map<String, Object> facts(AiService.Analysis analysis) {
        Map<String, Object> facts = new LinkedHashMap<>();
        putMeasure(facts, "plotArea", analysis.plotAreaSqm(), "sqm");
        putMeasure(facts, "roadWidth", analysis.roadWidthM(), "m");
        putMeasure(facts, "plotFrontage", analysis.plotFrontageM(), "m");
        putMeasure(facts, "buildingHeight", analysis.buildingHeightM(), "m");
        putMeasure(facts, "builtUpArea", analysis.builtUpAreaSqm(), "sqm");
        if (analysis.buildingUse() != null) facts.put("buildingUse", analysis.buildingUse());
        if (analysis.electricLineCategory() != null) {
            facts.put("electricLineCategory", analysis.electricLineCategory());
        }
        if (analysis.buildingForm() != null) facts.put("buildingForm", analysis.buildingForm());
        if (analysis.storeys() != null) facts.put("storeys", analysis.storeys());
        return facts;
    }

    private static void putMeasure(Map<String, Object> target, String name, Double value, String unit) {
        if (value != null) target.put(name, Map.of("value", value, "unit", unit));
    }

    private static String pages(RulePassage passage) {
        return passage.getPageStart() == passage.getPageEnd()
                ? Integer.toString(passage.getPageStart())
                : passage.getPageStart() + "-" + passage.getPageEnd();
    }

    public record CitedQuote(String chunkId, String quote) {
    }
    public record Claim(String text,List<String> chunkIds) {}

    public record Proposal(
            String status,
            String language,
            String answer,
            List<CitedQuote> citations,
            List<String> missingInputs,
            Map<String, Object> calculations,
            String model,
            List<Claim> claims) {
        public Proposal(String status,String language,String answer,List<CitedQuote> citations,List<String> missingInputs,
                Map<String,Object> calculations,String model) {
            this(status,language,answer,citations,missingInputs,calculations,model,List.of());
        }
    }
}

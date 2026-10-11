package org.springboot.backend.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class JsonlParser {

    private final JsonMapper json;

    public JsonlParser(JsonMapper json) {
        this.json = json;
    }

    public List<RuleRecord> parse(InputStream input) {
        List<RuleRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                JsonNode node = json.readTree(line);
                records.add(new RuleRecord(
                        required(node, "rule", lineNumber),
                        required(node, "title", lineNumber),
                        required(node, "part", lineNumber),
                        requiredInt(node, "page_start", lineNumber),
                        requiredInt(node, "page_end", lineNumber),
                        required(node, "text", lineNumber)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read rules JSONL", e);
        }
        return List.copyOf(records);
    }

    private static String required(JsonNode node, String field, int line) {
        String value = node.path(field).asString("").strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Missing " + field + " at JSONL line " + line);
        }
        return value;
    }

    private static int requiredInt(JsonNode node, String field, int line) {
        int value = node.path(field).asInt(0);
        if (value <= 0) {
            throw new IllegalArgumentException("Invalid " + field + " at JSONL line " + line);
        }
        return value;
    }

    public record RuleRecord(
            String rule,
            String title,
            String part,
            int pageStart,
            int pageEnd,
            String text) {
    }
}

package org.springboot.backend.rules;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Loads all rules from {@code bhumi_vikas_rules.jsonl} at startup, cleans them and
 * splits them into chunks for search.
 */
@Component
public class RuleRepository {

    /**
     * Rules inserted by amendment that the PDF prints inside the previous rule.
     * Each pattern matches the start of the inserted rule in the parent's cleaned text.
     */
    private static final List<InsertedRuleSpec> INSERTED_RULES = List.of(
            new InsertedRuleSpec("12-A", "12", "Instant/deemed approval for small residential plots", "(?m)^[ \\t]*12\\s*A\\.\\s"),
            new InsertedRuleSpec("17-a", "17", "Agricultural tourist facility", "(?m)^[ \\t]*\\[17-a\\."),
            new InsertedRuleSpec("25(A)", "25", "Appeal and revision", "(?m)^[ \\t]*\\[25\\(A\\)\\."),
            new InsertedRuleSpec("26-A", "26", "Authorised Architect/Structural Engineer to grant permission", "(?m)^[ \\t]*\\[26\\.A\\."),
            new InsertedRuleSpec("42-A", "42", "Norms for Information Technology", "(?m)^[ \\t]*\\[42A\\."),
            new InsertedRuleSpec("93(a)", "93", "Dual water piping system", "(?m)^[ \\t]*\\[\"93\\(a\\)"));

    private final Map<String, Rule> rulesByKey = new LinkedHashMap<>();
    private final Map<String, InsertedRule> insertedByKey = new LinkedHashMap<>();
    private final List<Chunk> chunks = new ArrayList<>();

    @Autowired
    public RuleRepository(@Value("${bvs.data-dir}") String dataDir) {
        this(Path.of(dataDir, "bhumi_vikas_rules.jsonl"));
    }

    public RuleRepository(Path rulesFile) {
        JsonMapper json = JsonMapper.builder().build();
        List<String> lines;
        try {
            lines = Files.readAllLines(rulesFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read rules file " + rulesFile.toAbsolutePath(), e);
        }
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            JsonNode node = json.readTree(line);
            Rule rule = buildRule(
                    node.get("rule").asString(),
                    node.get("title").asString(),
                    node.get("part").asString(),
                    node.get("page_start").asInt(),
                    node.get("page_end").asInt(),
                    node.get("text").asString());
            rulesByKey.put(key(rule.number()), rule);
            rule.insertedRules().forEach(r -> insertedByKey.put(key(r.id()), r));
            chunks.addAll(RuleChunker.chunk(rule));
        }
    }

    private static Rule buildRule(String number, String title, String part, int pageStart, int pageEnd, String rawText) {
        RuleTextCleaner.CleanedText cleaned = RuleTextCleaner.clean(rawText, pageStart);
        List<InsertedRule> inserted = new ArrayList<>();
        for (InsertedRuleSpec spec : INSERTED_RULES) {
            if (!spec.parentRule.equals(number)) {
                continue;
            }
            Matcher m = Pattern.compile(spec.startPattern).matcher(cleaned.text());
            if (!m.find()) {
                throw new IllegalStateException("Inserted rule " + spec.id + " not found inside rule " + number);
            }
            inserted.add(new InsertedRule(spec.id, number, spec.title, m.start(), cleaned.pageAt(m.start())));
        }
        return new Rule(number, title, part, pageStart, pageEnd, cleaned.text(), List.copyOf(inserted), cleaned);
    }

    /** Finds a rule by number. Accepts "56", "Rule 26-B", "26b", and inserted ids like "17-a" (returns rule 17). */
    public Optional<Rule> find(String number) {
        String k = key(number);
        Rule rule = rulesByKey.get(k);
        if (rule == null && insertedByKey.containsKey(k)) {
            rule = rulesByKey.get(key(insertedByKey.get(k).parentRule()));
        }
        return Optional.ofNullable(rule);
    }

    /** The inserted rule for ids like "17-a" or "25(A)", if the number names one. */
    public Optional<InsertedRule> findInserted(String number) {
        return Optional.ofNullable(insertedByKey.get(key(number)));
    }

    public Collection<Rule> all() {
        return rulesByKey.values();
    }

    public List<Chunk> chunks() {
        return chunks;
    }

    /** "Rule 26-B" -> "26B", "25(A)" -> "25A", "17-a" -> "17A". */
    static String key(String number) {
        return number.toUpperCase(Locale.ROOT)
                .replaceFirst("^\\s*RULE\\s*", "")
                .replaceAll("[\\s\\-().\\[\\]\"]", "");
    }

    private record InsertedRuleSpec(String id, String parentRule, String title, String startPattern) {
    }
}

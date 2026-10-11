package org.springboot.backend.llm;

import java.util.Map;

/**
 * A language model that returns JSON matching a schema. Claude is the default
 * implementation; another provider (e.g. Cerebras) can be plugged in by implementing this.
 */
public interface LlmClient {

    /** How hard the model should think. Low for quick extraction, medium for answers. */
    enum Effort { low, medium, high }

    boolean isConfigured();

    /** Name shown in logs and responses, e.g. "claude-opus-5-5". */
    String modelName();

    /**
     * Sends one system prompt and one user message and returns the model's JSON text.
     *
     * @param jsonSchema JSON Schema the reply must follow
     * @throws LlmException on HTTP errors, refusals or truncated output
     */
    String completeJson(String systemPrompt, String userMessage, Map<String, Object> jsonSchema, Effort effort);
}

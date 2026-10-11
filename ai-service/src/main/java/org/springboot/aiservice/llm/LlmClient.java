package org.springboot.aiservice.llm;

import java.util.Map;

/** Provider-neutral structured generation contract, owned only by the AI service. */
public interface LlmClient {

    enum Effort { low, medium, high }

    boolean isConfigured();

    String modelName();

    String completeJson(String systemPrompt, String userMessage, Map<String, Object> jsonSchema, Effort effort);
}

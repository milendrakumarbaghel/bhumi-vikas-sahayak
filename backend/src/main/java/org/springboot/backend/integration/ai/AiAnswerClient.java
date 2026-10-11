package org.springboot.backend.integration.ai;

import java.util.List;
import java.util.Map;

/**
 * Internal contract with the AI service for answer wording. The reply is an unverified
 * proposal: the backend checks every quote against the rule text before showing it.
 */
public interface AiAnswerClient {

    Proposal answer(Request request);

    /**
     * @param language    "en" or "hi" - the language the answer must be written in
     * @param calculation backend-computed result, or null when no calculator applies
     */
    record Request(String question, String language, List<ChunkText> chunks, Map<String, Object> calculation) {
    }

    record ChunkText(String id, String label, String title, String pages, String text) {
    }

    /** @param status "answered", "not_found" or "needs_more_info" */
    record Proposal(String status, String language, String answer, List<CitedQuote> citations,
                    List<String> missingInputs, String model) {
    }

    record CitedQuote(String chunkId, String quote) {
    }
}

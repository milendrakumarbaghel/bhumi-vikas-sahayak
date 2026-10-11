package org.springboot.backend.integration.ai;

import java.util.List;

/**
 * Internal contract with the separately deployed AI service. The backend owns
 * legal retrieval and validation; this service only proposes query intent.
 */
public interface AiIntentClient {

    boolean isEnabled();

    Analysis analyse(String question);

    record Analysis(
            String language,
            String englishQuestion,
            String searchTerms,
            List<String> topics,
            Measure plotArea,
            Measure roadWidth,
            Measure buildingHeight,
            Measure builtUpArea,
            String buildingUse,
            List<String> ruleRefs,
            String model) {
    }

    record Measure(Double value, String unit) {
    }
}

package org.springboot.backend.service;

import java.util.List;

public interface AiService {

    Analysis analyse(String question);

    record Analysis(
            String language,
            String englishQuestion,
            String searchTerms,
            List<String> topics,
            Double plotAreaSqm,
            Double roadWidthM,
            Double buildingHeightM,
            Double builtUpAreaSqm,
            String buildingUse,
            List<String> ruleReferences,
            String source) {
    }
}

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
            Double plotFrontageM,
            Double buildingHeightM,
            Double builtUpAreaSqm,
            String buildingUse,
            String electricLineCategory,
            String buildingForm,
            List<String> ruleReferences,
            String source,
            Integer storeys) {
        public Analysis(String language,String englishQuestion,String searchTerms,List<String> topics,
                Double plotAreaSqm,Double roadWidthM,Double plotFrontageM,Double buildingHeightM,Double builtUpAreaSqm,
                String buildingUse,String electricLineCategory,String buildingForm,List<String> ruleReferences,String source) {
            this(language,englishQuestion,searchTerms,topics,plotAreaSqm,roadWidthM,plotFrontageM,buildingHeightM,
                    builtUpAreaSqm,buildingUse,electricLineCategory,buildingForm,ruleReferences,source,null);
        }
    }
}

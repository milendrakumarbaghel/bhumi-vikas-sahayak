package org.springboot.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springboot.backend.entity.BuildingRule;
import org.springboot.backend.entity.RulePassage;
import org.springboot.backend.integration.ai.GroundedAiClient;
import org.springboot.backend.service.GeneratedAnswerValidationService;
import tools.jackson.databind.json.JsonMapper;

class GeneratedAnswerValidationServiceTest {

    @Test
    void everySentenceMustHaveVerifiedClaimEvidence() {
        var rule=new BuildingRule("83","Lifts","Part V",95,95,"text");
        var passage=new RulePassage("Rule 83",95,95,"Provision for lifts shall be made for building more than 12.5 meters in height.");
        rule.addPassage(passage);
        String id=GroundedAiClient.chunkId(passage);
        var proposal=new GroundedAiClient.Proposal("answered","en","A lift is required. Approval is automatic.",
                List.of(new GroundedAiClient.CitedQuote(id,passage.getText())),List.of(),Map.of(),"test",
                List.of(new GroundedAiClient.Claim("A lift is required.",List.of(id))));
        var validation=new GeneratedAnswerValidationService(JsonMapper.builder().build()).validate(proposal,List.of(passage),"15 m building",Map.of());
        assertThat(validation.passed()).isFalse();
        assertThat(validation.problems()).contains("claims do not cover the entire answer in order");
    }

    @Test
    void acceptsExactQuoteAndCalculatedNumberButRejectsInventedNumber() {
        BuildingRule rule = new BuildingRule("56", "Residential Buildings", "Part V", 77, 78, "text");
        RulePassage passage = new RulePassage("Rule 56 (1)", 77, 77,
                "More than 9.0 meters and up to 12 meters 3.6 meters");
        rule.addPassage(passage);
        String id = GroundedAiClient.chunkId(passage);
        Map<String, Object> calculation = Map.of("result", Map.of("value", 3.6, "unit", "m"));
        var verifier = new GeneratedAnswerValidationService(JsonMapper.builder().build());

        var valid = verifier.validate(new GroundedAiClient.Proposal(
                        "answered", "en", "Rule 56 requires 3.6 meters.",
                        List.of(new GroundedAiClient.CitedQuote(id,
                                "More than 9.0 meters and up to 12 meters 3.6 meters")),
                        List.of(), calculation, "test",List.of(new GroundedAiClient.Claim("Rule 56 requires 3.6 meters.",List.of(id)))),
                List.of(passage), "The road is 10 m wide.", calculation);
        assertThat(valid.passed()).as(valid.problems().toString()).isTrue();

        var invalid = verifier.validate(new GroundedAiClient.Proposal(
                        "answered", "en", "Rule 56 requires 7.7 meters.",
                        List.of(new GroundedAiClient.CitedQuote(id,
                                "More than 9.0 meters and up to 12 meters 3.6 meters")),
                        List.of(), calculation, "test",List.of(new GroundedAiClient.Claim("Rule 56 requires 7.7 meters.",List.of(id)))),
                List.of(passage), "The road is 10 m wide.", calculation);
        assertThat(invalid.passed()).isFalse();
        assertThat(invalid.problems()).anyMatch(value -> value.contains("ungrounded number"));
    }
}

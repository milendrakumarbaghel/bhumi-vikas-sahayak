package org.springboot.backend.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springboot.backend.integration.ai.AiIntentClient;
import org.springboot.backend.integration.ai.AiServiceException;

class QueryNormalizerAiServiceTest {

    @Test
    void verifiesAiMeasurementsAgainstTheOriginalQuestion() {
        AiIntentClient client = new AiIntentClient() {
            public boolean isEnabled() { return true; }

            public Analysis analyse(String question) {
                return new Analysis(
                        "en",
                        question,
                        "residential front open space",
                        List.of("SETBACK", "UNKNOWN_FROM_NEWER_SERVICE"),
                        new Measure(200.0, "sqm"),
                        new Measure(9.0, "m"),
                        new Measure(99.0, "m"),
                        null,
                        "residential",
                        List.of("56"),
                        "test-model");
            }
        };

        QueryIntent intent = new QueryNormalizer(client)
                .normalize("My residential plot is 200 sqm on a 9 m road");

        assertThat(intent.source()).isEqualTo("ai-service");
        assertThat(intent.topics()).containsExactly(Topic.SETBACK);
        assertThat(intent.plotAreaSqm()).isEqualTo(200.0);
        assertThat(intent.roadWidthM()).isEqualTo(9.0);
        assertThat(intent.buildingHeightM()).isNull();
    }

    @Test
    void fallsBackWhenAiServiceIsUnavailable() {
        AiIntentClient client = new AiIntentClient() {
            public boolean isEnabled() { return true; }

            public Analysis analyse(String question) {
                throw new AiServiceException("unavailable");
            }
        };

        QueryIntent intent = new QueryNormalizer(client)
                .normalize("What is the fee for building permission?");

        assertThat(intent.source()).isEqualTo("offline");
        assertThat(intent.topics()).contains(Topic.FEES);
    }
}

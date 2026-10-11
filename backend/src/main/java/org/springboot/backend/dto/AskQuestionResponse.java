package org.springboot.backend.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AskQuestionResponse(
        UUID answerId,
        String status,
        String language,
        LocalDate asOfDate,
        String sourceVersion,
        String sourceDocumentUrl,
        String answer,
        List<CitationResponse> citations,
        Map<String, Object> calculation,
        List<String> warnings,
        String disclaimer) {
}

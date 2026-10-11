package org.springboot.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskQuestionRequest(
        @NotBlank @Size(min = 3, max = 2000) String question,
        String language,
        LocalDate asOfDate) {
}

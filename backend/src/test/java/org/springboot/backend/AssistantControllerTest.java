package org.springboot.backend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springboot.backend.controller.AssistantController;
import org.springboot.backend.dto.AskQuestionResponse;
import org.springboot.backend.exception.GlobalExceptionHandler;
import org.springboot.backend.service.AssistantService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AssistantControllerTest {

    @Test
    void exposesStableAnswerContractAndValidatesRequests() throws Exception {
        AssistantService service = mock(AssistantService.class);
        when(service.answer(any())).thenReturn(new AskQuestionResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000001"), "ANSWERED", "en",
                LocalDate.of(2026, 10, 11), "rules through 2025", "/api/v1/documents/rules.pdf",
                "Answer", List.of(), Map.of(), List.of(), "Not legal advice"));
        var mvc = MockMvcBuilders.standaloneSetup(new AssistantController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(post("/api/v1/questions:answer").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What does Rule 56 require?\",\"language\":\"en\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ANSWERED"))
                .andExpect(jsonPath("$.sourceDocumentUrl").value("/api/v1/documents/rules.pdf"));
        mvc.perform(post("/api/v1/questions:answer").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"\"}"))
                .andExpect(status().isUnprocessableContent());
    }
}

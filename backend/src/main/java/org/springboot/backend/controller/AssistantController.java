package org.springboot.backend.controller;

import org.springboot.backend.dto.AskQuestionRequest;
import org.springboot.backend.dto.AskQuestionResponse;
import org.springboot.backend.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AssistantController {

    private final AssistantService assistant;

    public AssistantController(AssistantService assistant) {
        this.assistant = assistant;
    }

    @PostMapping("/questions:answer")
    public AskQuestionResponse answer(@Valid @RequestBody AskQuestionRequest request) {
        return assistant.answer(request);
    }
}

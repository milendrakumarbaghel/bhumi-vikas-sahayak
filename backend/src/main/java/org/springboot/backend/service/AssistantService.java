package org.springboot.backend.service;

import org.springboot.backend.dto.AskQuestionRequest;
import org.springboot.backend.dto.AskQuestionResponse;

public interface AssistantService {
    AskQuestionResponse answer(AskQuestionRequest request);
}

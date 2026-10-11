package org.springboot.backend.application;

import org.springboot.backend.retrieval.Retriever;
import org.springframework.stereotype.Service;

/** Use-case boundary for deterministic rule retrieval. */
@Service
public class RuleQueryService {

    private final Retriever retriever;

    public RuleQueryService(Retriever retriever) {
        this.retriever = retriever;
    }

    public Retriever.Result search(String question) {
        return retriever.retrieve(question);
    }
}

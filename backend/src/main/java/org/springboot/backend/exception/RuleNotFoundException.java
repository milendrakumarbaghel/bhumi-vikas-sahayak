package org.springboot.backend.exception;

public class RuleNotFoundException extends RuntimeException {

    public RuleNotFoundException(String ruleNumber) {
        super("Rule " + ruleNumber + " was not found");
    }
}

package org.springboot.backend.exception;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuleNotFoundException.class)
    ProblemDetail handleRuleNotFound(RuleNotFoundException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setType(URI.create("urn:bvs:problem:rule-not-found"));
        detail.setTitle("Rule not found");
        detail.setProperty("code", "RULE_NOT_FOUND");
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT, "Request validation failed");
        detail.setType(URI.create("urn:bvs:problem:validation"));
        detail.setTitle("Validation failed");
        detail.setProperty("code", "VALIDATION_ERROR");
        detail.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage() == null ? "invalid" : error.getDefaultMessage()))
                .toList());
        return detail;
    }
}

package org.springboot.backend.integration.ai;

/** Raised when the optional, separately deployed AI service cannot analyse a query. */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.nilesh.knowledgebase.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nilesh.knowledgebase.dto.ApiResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleLlmUnavailableReturns503() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleLlmUnavailable(
                new LlmServiceUnavailableException("down"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("AI service temporarily unavailable", response.getBody().message());
    }

    @Test
    void handleLlmTimeoutReturns504() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleLlmTimeout(
                new LlmTimeoutException("timeout"));

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, response.getStatusCode());
        assertEquals("Answer generation timed out", response.getBody().message());
    }
}

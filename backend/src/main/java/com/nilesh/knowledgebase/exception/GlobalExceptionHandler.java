package com.nilesh.knowledgebase.exception;

import java.util.List;
import java.util.stream.Collectors;

import com.nilesh.knowledgebase.dto.ApiResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(exception.getMessage()));
	}

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(exception.getMessage()));
	}

	@ExceptionHandler({BadCredentialsException.class, AuthenticationException.class, JwtAuthenticationException.class})
	public ResponseEntity<ApiResponse<Void>> handleAuthentication(Exception exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication failed"));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
		List<String> errors = exception.getBindingResult().getFieldErrors().stream()
			.map(this::formatFieldError)
			.collect(Collectors.toList());
		return ResponseEntity.badRequest().body(ApiResponse.error(String.join(", ", errors)));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Request could not be processed"));
	}

	@ExceptionHandler(com.nilesh.knowledgebase.exception.LlmServiceUnavailableException.class)
	public ResponseEntity<ApiResponse<Void>> handleLlmUnavailable(
			com.nilesh.knowledgebase.exception.LlmServiceUnavailableException exception) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(ApiResponse.error("AI service temporarily unavailable"));
	}

	@ExceptionHandler(com.nilesh.knowledgebase.exception.LlmTimeoutException.class)
	public ResponseEntity<ApiResponse<Void>> handleLlmTimeout(
			com.nilesh.knowledgebase.exception.LlmTimeoutException exception) {
		return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
				.body(ApiResponse.error("Answer generation timed out"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Unexpected server error"));
	}

	private String formatFieldError(FieldError fieldError) {
		return fieldError.getField() + ": " + fieldError.getDefaultMessage();
	}
}
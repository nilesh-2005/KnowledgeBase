package com.nilesh.knowledgebase.exception;

import org.springframework.security.authentication.AuthenticationServiceException;

public class JwtAuthenticationException extends AuthenticationServiceException {

	public JwtAuthenticationException(String message) {
		super(message);
	}
}
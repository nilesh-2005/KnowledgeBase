package com.nilesh.knowledgebase.service;

import com.nilesh.knowledgebase.dto.AuthResponse;
import com.nilesh.knowledgebase.dto.LoginRequest;
import com.nilesh.knowledgebase.dto.RegisterRequest;

public interface AuthService {

	AuthResponse register(RegisterRequest request);
	AuthResponse login(LoginRequest request);
}
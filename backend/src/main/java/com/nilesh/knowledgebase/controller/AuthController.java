package com.nilesh.knowledgebase.controller;

import com.nilesh.knowledgebase.dto.ApiResponse;
import com.nilesh.knowledgebase.dto.AuthResponse;
import com.nilesh.knowledgebase.dto.LoginRequest;
import com.nilesh.knowledgebase.dto.RegisterRequest;
import com.nilesh.knowledgebase.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponse.success("Registration successful", authService.register(request)));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
	}
}
package com.nilesh.knowledgebase.service;

import java.util.Locale;

import com.nilesh.knowledgebase.dto.AuthResponse;
import com.nilesh.knowledgebase.dto.LoginRequest;
import com.nilesh.knowledgebase.dto.RegisterRequest;
import com.nilesh.knowledgebase.dto.UserResponse;
import com.nilesh.knowledgebase.entity.Role;
import com.nilesh.knowledgebase.entity.User;
import com.nilesh.knowledgebase.exception.DuplicateEmailException;
import com.nilesh.knowledgebase.repository.UserRepository;
import com.nilesh.knowledgebase.security.JwtService;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	@Override
	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String normalizedEmail = normalizeEmail(request.email());
		if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
			throw new DuplicateEmailException("Email is already registered");
		}

		Role role = userRepository.count() == 0 ? Role.ADMIN : Role.VIEWER;
		User user = User.builder()
			.fullName(request.fullName().trim())
			.email(normalizedEmail)
			.password(passwordEncoder.encode(request.password()))
			.role(role)
			.build();

		User savedUser = userRepository.save(user);
		return new AuthResponse("Bearer", jwtService.generateToken(savedUser), toUserResponse(savedUser));
	}

	@Override
	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String normalizedEmail = normalizeEmail(request.email());
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));
		} catch (Exception exception) {
			throw new BadCredentialsException("Invalid email or password");
		}

		User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
			.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
		return new AuthResponse("Bearer", jwtService.generateToken(user), toUserResponse(user));
	}

	private UserResponse toUserResponse(User user) {
		return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getCreatedAt(), user.getUpdatedAt());
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
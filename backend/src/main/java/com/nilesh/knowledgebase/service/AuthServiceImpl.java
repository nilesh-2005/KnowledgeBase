package com.nilesh.knowledgebase.service;

import com.nilesh.knowledgebase.dto.AuthResponse;
import com.nilesh.knowledgebase.dto.LoginRequest;
import com.nilesh.knowledgebase.dto.RegisterRequest;
import com.nilesh.knowledgebase.entity.Role;
import com.nilesh.knowledgebase.entity.User;
import com.nilesh.knowledgebase.exception.DuplicateEmailException;
import com.nilesh.knowledgebase.mapper.UserMapper;
import com.nilesh.knowledgebase.repository.UserRepository;
import com.nilesh.knowledgebase.security.JwtService;
import com.nilesh.knowledgebase.util.EmailNormalizer;

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
	private final UserMapper userMapper;
	private final EmailNormalizer emailNormalizer;

	@Override
	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String normalizedEmail = emailNormalizer.normalize(request.email());
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
		return new AuthResponse("Bearer", jwtService.generateToken(savedUser), userMapper.toResponse(savedUser));
	}

	@Override
	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String normalizedEmail = emailNormalizer.normalize(request.email());
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));
		} catch (Exception exception) {
			throw new BadCredentialsException("Invalid email or password");
		}

		User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
			.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
		return new AuthResponse("Bearer", jwtService.generateToken(user), userMapper.toResponse(user));
	}
}

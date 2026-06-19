package com.nilesh.knowledgebase.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nilesh.knowledgebase.dto.UpdateUserRequest;
import com.nilesh.knowledgebase.dto.UserResponse;
import com.nilesh.knowledgebase.entity.Role;
import com.nilesh.knowledgebase.entity.User;
import com.nilesh.knowledgebase.exception.DuplicateEmailException;
import com.nilesh.knowledgebase.exception.ResourceNotFoundException;
import com.nilesh.knowledgebase.repository.UserRepository;
import com.nilesh.knowledgebase.security.UserPrincipal;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserResponse getCurrentUser(UserPrincipal principal) {
		return toUserResponse(loadCurrentUser(principal));
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponse> getAllUsers(UserPrincipal principal) {
		requireAdmin(principal);
		return userRepository.findAll().stream().map(this::toUserResponse).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getUserById(UUID id, UserPrincipal principal) {
		requireSelfOrAdmin(id, principal);
		return toUserResponse(findById(id));
	}

	@Override
	@Transactional
	public UserResponse updateUser(UUID id, UpdateUserRequest request, UserPrincipal principal) {
		User user = findById(id);
		if (!isAdmin(principal) && !user.getId().equals(principal.getId())) {
			throw new AccessDeniedException("You cannot update this user");
		}

		String normalizedEmail = normalizeEmail(request.email());
		userRepository.findByEmailIgnoreCase(normalizedEmail)
			.filter(existing -> !existing.getId().equals(user.getId()))
			.ifPresent(existing -> {
				throw new DuplicateEmailException("Email is already registered");
			});

		user.setFullName(request.fullName().trim());
		user.setEmail(normalizedEmail);
		if (isAdmin(principal) && request.role() != null) {
			user.setRole(request.role());
		}

		return toUserResponse(userRepository.save(user));
	}

	@Override
	@Transactional
	public void deleteUser(UUID id, UserPrincipal principal) {
		requireAdmin(principal);
		if (!userRepository.existsById(id)) {
			throw new ResourceNotFoundException("User not found");
		}
		userRepository.deleteById(id);
	}

	private User loadCurrentUser(UserPrincipal principal) {
		return userRepository.findById(principal.getId())
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private User findById(UUID id) {
		return userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private void requireSelfOrAdmin(UUID id, UserPrincipal principal) {
		if (!isAdmin(principal) && !id.equals(principal.getId())) {
			throw new AccessDeniedException("You cannot access this user");
		}
	}

	private void requireAdmin(UserPrincipal principal) {
		if (!isAdmin(principal)) {
			throw new AccessDeniedException("Admin privileges are required");
		}
	}

	private boolean isAdmin(UserPrincipal principal) {
		return principal != null && principal.getRole() == Role.ADMIN;
	}

	private UserResponse toUserResponse(User user) {
		return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getCreatedAt(), user.getUpdatedAt());
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
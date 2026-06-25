package com.nilesh.knowledgebase.service;

import java.util.UUID;

import com.nilesh.knowledgebase.dto.PageResponse;
import com.nilesh.knowledgebase.dto.UpdateUserRequest;
import com.nilesh.knowledgebase.dto.UserResponse;
import com.nilesh.knowledgebase.entity.Role;
import com.nilesh.knowledgebase.entity.User;
import com.nilesh.knowledgebase.entity.AuditAction;
import com.nilesh.knowledgebase.exception.DuplicateEmailException;
import com.nilesh.knowledgebase.exception.ResourceNotFoundException;
import com.nilesh.knowledgebase.mapper.UserMapper;
import com.nilesh.knowledgebase.repository.UserRepository;
import com.nilesh.knowledgebase.security.UserPrincipal;
import com.nilesh.knowledgebase.util.EmailNormalizer;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final EmailNormalizer emailNormalizer;
	private final AuditLogService auditLogService;

	@Override
	@Transactional(readOnly = true)
	public UserResponse getCurrentUser(UserPrincipal principal) {
		return userMapper.toResponse(loadCurrentUser(principal));
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<UserResponse> getAllUsers(UserPrincipal principal, Pageable pageable) {
		requireAdmin(principal);
		return PageResponse.from(userRepository.findAll(pageable).map(userMapper::toResponse));
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getUserById(UUID id, UserPrincipal principal) {
		requireSelfOrAdmin(id, principal);
		return userMapper.toResponse(findById(id));
	}

	@Override
	@Transactional
	public UserResponse updateUser(UUID id, UpdateUserRequest request, UserPrincipal principal) {
		User user = findById(id);
		if (!isAdmin(principal) && !user.getId().equals(principal.getId())) {
			throw new AccessDeniedException("You cannot update this user");
		}

		String normalizedEmail = emailNormalizer.normalize(request.email());
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

		return userMapper.toResponse(userRepository.save(user));
	}

	@Override
	@Transactional
	public void deleteUser(UUID id, UserPrincipal principal) {
		requireAdmin(principal);
		if (!userRepository.existsById(id)) {
			throw new ResourceNotFoundException("User not found");
		}
		userRepository.deleteById(id);
		
		auditLogService.logAction(AuditAction.USER_DELETE, principal.getId(), id, "Deleted user");
	}

	@Override
	@Transactional
	public UserResponse changeUserRole(UUID id, Role role, UserPrincipal principal) {
		requireAdmin(principal);
		User user = findById(id);
		if (user.getId().equals(principal.getId())) {
			throw new AccessDeniedException("Admin cannot change their own role");
		}
		user.setRole(role);
		User savedUser = userRepository.save(user);
		
		auditLogService.logAction(AuditAction.USER_ROLE_CHANGE, principal.getId(), id, "Changed role to: " + role.name());
		
		return userMapper.toResponse(savedUser);
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

}

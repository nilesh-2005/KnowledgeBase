package com.nilesh.knowledgebase.service;

import java.util.UUID;

import com.nilesh.knowledgebase.dto.PageResponse;
import com.nilesh.knowledgebase.dto.UpdateUserRequest;
import com.nilesh.knowledgebase.dto.UserResponse;
import com.nilesh.knowledgebase.security.UserPrincipal;

import com.nilesh.knowledgebase.entity.Role;
import org.springframework.data.domain.Pageable;

public interface UserService {

	UserResponse getCurrentUser(UserPrincipal principal);
	PageResponse<UserResponse> getAllUsers(UserPrincipal principal, Pageable pageable);
	UserResponse getUserById(UUID id, UserPrincipal principal);
	UserResponse updateUser(UUID id, UpdateUserRequest request, UserPrincipal principal);
	void deleteUser(UUID id, UserPrincipal principal);
	UserResponse changeUserRole(UUID id, Role role, UserPrincipal principal);
}

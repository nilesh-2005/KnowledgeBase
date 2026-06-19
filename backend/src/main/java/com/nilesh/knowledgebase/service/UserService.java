package com.nilesh.knowledgebase.service;

import java.util.List;
import java.util.UUID;

import com.nilesh.knowledgebase.dto.UpdateUserRequest;
import com.nilesh.knowledgebase.dto.UserResponse;
import com.nilesh.knowledgebase.security.UserPrincipal;

public interface UserService {

	UserResponse getCurrentUser(UserPrincipal principal);
	List<UserResponse> getAllUsers(UserPrincipal principal);
	UserResponse getUserById(UUID id, UserPrincipal principal);
	UserResponse updateUser(UUID id, UpdateUserRequest request, UserPrincipal principal);
	void deleteUser(UUID id, UserPrincipal principal);
}
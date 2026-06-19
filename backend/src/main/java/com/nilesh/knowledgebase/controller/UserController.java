package com.nilesh.knowledgebase.controller;

import java.util.List;
import java.util.UUID;

import com.nilesh.knowledgebase.dto.ApiResponse;
import com.nilesh.knowledgebase.dto.UpdateUserRequest;
import com.nilesh.knowledgebase.dto.UserResponse;
import com.nilesh.knowledgebase.security.UserPrincipal;
import com.nilesh.knowledgebase.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Current user retrieved successfully", userService.getCurrentUser(principal)));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", userService.getAllUsers(principal)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userService.getUserById(id, principal)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> updateUser(
		@PathVariable UUID id,
		@Valid @RequestBody UpdateUserRequest request,
		@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("User updated successfully", userService.updateUser(id, request, principal)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
		userService.deleteUser(id, principal);
		return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
	}
}
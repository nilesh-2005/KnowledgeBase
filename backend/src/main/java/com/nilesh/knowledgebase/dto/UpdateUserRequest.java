package com.nilesh.knowledgebase.dto;

import com.nilesh.knowledgebase.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
	@NotBlank(message = "fullName is required")
	String fullName,

	@NotBlank(message = "email is required")
	@Email(message = "email must be valid")
	String email,

	Role role
) {
}
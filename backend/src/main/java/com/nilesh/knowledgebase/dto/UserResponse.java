package com.nilesh.knowledgebase.dto;

import java.time.Instant;
import java.util.UUID;

import com.nilesh.knowledgebase.entity.Role;

public record UserResponse(
	UUID id,
	String fullName,
	String email,
	Role role,
	Instant createdAt,
	Instant updatedAt
) {
}
package com.nilesh.knowledgebase.mapper;

import com.nilesh.knowledgebase.dto.UserResponse;
import com.nilesh.knowledgebase.entity.User;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

	public UserResponse toResponse(User user) {
		return new UserResponse(
			user.getId(),
			user.getFullName(),
			user.getEmail(),
			user.getRole(),
			user.getCreatedAt(),
			user.getUpdatedAt());
	}
}

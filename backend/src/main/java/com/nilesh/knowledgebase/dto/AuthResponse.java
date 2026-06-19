package com.nilesh.knowledgebase.dto;

public record AuthResponse(String tokenType, String token, UserResponse user) {
}
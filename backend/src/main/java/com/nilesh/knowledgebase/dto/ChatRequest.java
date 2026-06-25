package com.nilesh.knowledgebase.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "question is required")
        @Size(min = 3, max = 2000, message = "question must be between 3 and 2000 characters")
        String question,

        @Min(value = 1, message = "topK must be at least 1")
        @Max(value = 10, message = "topK must be at most 10")
        Integer topK
) {}

package com.nilesh.knowledgebase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDto {
    private UUID id;
    
    @NotBlank
    private String name;
    
    private String description;
    
    private UUID ownerId;
    
    private Instant createdAt;
    private Instant updatedAt;
}

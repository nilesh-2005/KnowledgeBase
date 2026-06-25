package com.nilesh.knowledgebase.dto;

import com.nilesh.knowledgebase.entity.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
    @NotBlank
    private String title;
    
    private String description;
    
    @NotNull
    private Visibility visibility;
    
    private UUID collectionId;
    
    private Set<UUID> tagIds;
}

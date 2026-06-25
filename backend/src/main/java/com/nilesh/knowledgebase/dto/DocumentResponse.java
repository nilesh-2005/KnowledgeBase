package com.nilesh.knowledgebase.dto;

import com.nilesh.knowledgebase.entity.Visibility;
import com.nilesh.knowledgebase.entity.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private UUID id;
    private String title;
    private String description;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Visibility visibility;
    private UUID ownerId;
    private CollectionDto collection;
    private Set<TagDto> tags;
    private DocumentStatus status;
    private String errorMessage;
    private Integer chunkCount;
    private Instant createdAt;
    private Instant updatedAt;
}

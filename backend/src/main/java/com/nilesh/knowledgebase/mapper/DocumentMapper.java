package com.nilesh.knowledgebase.mapper;

import com.nilesh.knowledgebase.dto.DocumentResponse;
import com.nilesh.knowledgebase.entity.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DocumentMapper {

    private final TagMapper tagMapper;
    private final CollectionMapper collectionMapper;

    public DocumentResponse toResponse(Document document) {
        if (document == null) {
            return null;
        }

        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .description(document.getDescription())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .visibility(document.getVisibility())
                .ownerId(document.getOwner() != null ? document.getOwner().getId() : null)
                .collection(collectionMapper.toDto(document.getCollection()))
                .tags(document.getTags() != null 
                        ? document.getTags().stream().map(tagMapper::toDto).collect(Collectors.toSet())
                        : null)
                .status(document.getStatus())
                .errorMessage(document.getErrorMessage())
                .chunkCount(document.getChunks() != null ? document.getChunks().size() : 0)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}

package com.nilesh.knowledgebase.mapper;

import com.nilesh.knowledgebase.dto.CollectionDto;
import com.nilesh.knowledgebase.entity.Collection;
import org.springframework.stereotype.Component;

@Component
public class CollectionMapper {

    public CollectionDto toDto(Collection collection) {
        if (collection == null) {
            return null;
        }
        return CollectionDto.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .ownerId(collection.getOwner() != null ? collection.getOwner().getId() : null)
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }
}

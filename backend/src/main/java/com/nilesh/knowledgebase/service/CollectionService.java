package com.nilesh.knowledgebase.service;

import com.nilesh.knowledgebase.dto.CollectionDto;
import com.nilesh.knowledgebase.entity.AuditAction;
import com.nilesh.knowledgebase.entity.Collection;
import com.nilesh.knowledgebase.entity.Role;
import com.nilesh.knowledgebase.entity.User;
import com.nilesh.knowledgebase.exception.ResourceNotFoundException;
import com.nilesh.knowledgebase.mapper.CollectionMapper;
import com.nilesh.knowledgebase.repository.CollectionRepository;
import com.nilesh.knowledgebase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final CollectionMapper collectionMapper;
    private final AuditLogService auditLogService;

    /**
     * Returns all workspace collections — visible to every role.
     * Collections are workspace-level resources in a knowledge base system.
     */
    @Transactional(readOnly = true)
    public Page<CollectionDto> getAllCollections(Pageable pageable) {
        return collectionRepository.findAll(pageable)
                .map(collectionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public CollectionDto getCollectionById(UUID id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
        return collectionMapper.toDto(collection);
    }

    @Transactional
    public CollectionDto createCollection(UUID userId, CollectionDto collectionDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Collection collection = new Collection();
        collection.setName(collectionDto.getName());
        collection.setDescription(collectionDto.getDescription());
        collection.setOwner(user);

        Collection savedCollection = collectionRepository.save(collection);
        
        auditLogService.logAction(AuditAction.COLLECTION_CREATE, userId, savedCollection.getId(), "Created collection: " + collection.getName());

        return collectionMapper.toDto(savedCollection);
    }

    @Transactional
    public CollectionDto updateCollection(UUID userId, UUID id, CollectionDto collectionDto) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Collections are workspace-wide; any EMPLOYEE or ADMIN can edit.
        // The controller already enforces @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')").

        collection.setName(collectionDto.getName());
        collection.setDescription(collectionDto.getDescription());

        return collectionMapper.toDto(collectionRepository.save(collection));
    }

    @Transactional
    public void deleteCollection(UUID userId, UUID id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Owner can always delete. ADMIN can delete any collection.
        if (!collection.getOwner().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to delete this collection");
        }

        collectionRepository.deleteById(id);
        
        auditLogService.logAction(AuditAction.COLLECTION_DELETE, userId, id, "Deleted collection: " + collection.getName());
    }
}

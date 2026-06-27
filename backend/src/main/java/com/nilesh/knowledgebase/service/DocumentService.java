package com.nilesh.knowledgebase.service;

import com.nilesh.knowledgebase.dto.DocumentResponse;
import com.nilesh.knowledgebase.dto.DocumentUploadRequest;
import com.nilesh.knowledgebase.entity.Collection;
import com.nilesh.knowledgebase.entity.Document;
import com.nilesh.knowledgebase.entity.Role;
import com.nilesh.knowledgebase.entity.Tag;
import com.nilesh.knowledgebase.entity.User;
import com.nilesh.knowledgebase.entity.Visibility;
import com.nilesh.knowledgebase.entity.AuditAction;
import com.nilesh.knowledgebase.exception.ResourceNotFoundException;
import com.nilesh.knowledgebase.mapper.DocumentMapper;
import com.nilesh.knowledgebase.repository.CollectionRepository;
import com.nilesh.knowledgebase.repository.DocumentRepository;
import com.nilesh.knowledgebase.repository.TagRepository;
import com.nilesh.knowledgebase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CollectionRepository collectionRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final DocumentMapper documentMapper;
    private final AuditLogService auditLogService;
    private final DocumentProcessingService documentProcessingService;
    private final com.nilesh.knowledgebase.repository.DocumentChunkRepository documentChunkRepository;

    @Transactional
    public DocumentResponse uploadDocument(UUID userId, MultipartFile file, DocumentUploadRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String storedFileName = storageService.store(file);

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setFileName(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setStoragePath(storedFileName);
        document.setOwner(user);
        document.setVisibility(request.getVisibility());

        if (request.getCollectionId() != null) {
            Collection collection = collectionRepository.findById(request.getCollectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
            document.setCollection(collection);
        }

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (UUID tagId : request.getTagIds()) {
                tags.add(tagRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId)));
            }
            document.setTags(tags);
        }

        Document savedDocument = documentRepository.save(document);
        log.info("[Upload] DOCUMENT_SAVED  documentId={} file={}", savedDocument.getId(), document.getFileName());

        // IMPORTANT: Register the afterCommit callback BEFORE calling any @Async methods.
        // Calling an @Async service method (like auditLogService) inside this transaction
        // can cause the synchronization manager's state to be altered on some Spring versions,
        // which prevents afterCommit from firing reliably.
        // Both the audit log AND the processing trigger are deferred to afterCommit.
        final UUID savedId = savedDocument.getId();
        final String fileName = document.getFileName();

        log.info("[Upload] REGISTERING_AFTER_COMMIT  documentId={}", savedId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("[Upload] AFTER_COMMIT_FIRED  documentId={}", savedId);
                // Audit log is moved here so it never blocks or interferes with the transaction
                auditLogService.logAction(AuditAction.DOCUMENT_UPLOAD, userId, savedId,
                        "Uploaded document: " + fileName);
                log.info("[Upload] TRIGGERING_ASYNC_PROCESSING  documentId={}", savedId);
                documentProcessingService.processDocumentAsync(savedId);
            }

            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    log.warn("[Upload] TRANSACTION_ROLLED_BACK  documentId={} - async processing NOT triggered", savedId);
                }
            }
        });

        return documentMapper.toResponse(savedDocument);
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> getVisibleDocuments(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getRole() == Role.ADMIN) {
            return documentRepository.findAll(pageable).map(documentMapper::toResponse);
        }
        return documentRepository.findVisibleDocuments(userId, pageable)
                .map(documentMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<DocumentResponse> searchDocuments(UUID userId, String query, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getRole() == Role.ADMIN) {
            return documentRepository.searchAllDocuments(query, pageable).map(documentMapper::toResponse);
        }
        return documentRepository.searchDocuments(userId, query, pageable)
                .map(documentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(UUID userId, UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        User user = userRepository.findById(userId).orElseThrow();

        if (document.getVisibility() == Visibility.PRIVATE && 
            !document.getOwner().getId().equals(userId) && 
            user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to view this document");
        }

        return documentMapper.toResponse(document);
    }

    @Transactional(readOnly = true)
    public List<com.nilesh.knowledgebase.dto.DocumentChunkResponse> getDocumentChunks(UUID userId, UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        User user = userRepository.findById(userId).orElseThrow();

        if (document.getVisibility() == Visibility.PRIVATE && 
            !document.getOwner().getId().equals(userId) && 
            user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to view chunks for this document");
        }

        return documentChunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId).stream()
                .map(c -> new com.nilesh.knowledgebase.dto.DocumentChunkResponse(
                        c.getId(),
                        c.getChunkIndex(),
                        c.getContent(),
                        c.getCharacterStart(),
                        c.getCharacterEnd(),
                        c.getTokenCount(),
                        c.getEmbedding() != null
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource downloadDocumentResource(UUID userId, UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        User user = userRepository.findById(userId).orElseThrow();

        if (document.getVisibility() == Visibility.PRIVATE && 
            !document.getOwner().getId().equals(userId) && 
            user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to download this document");
        }

        return storageService.loadAsResource(document.getStoragePath());
    }

    @Transactional
    public DocumentResponse updateDocument(UUID userId, UUID documentId, DocumentUploadRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        User user = userRepository.findById(userId).orElseThrow();

        if (!document.getOwner().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to modify this document");
        }

        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setVisibility(request.getVisibility());

        if (request.getCollectionId() != null) {
            Collection collection = collectionRepository.findById(request.getCollectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
            document.setCollection(collection);
        } else {
            document.setCollection(null);
        }

        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>();
            for (UUID tagId : request.getTagIds()) {
                tags.add(tagRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId)));
            }
            document.setTags(tags);
        }

        return documentMapper.toResponse(documentRepository.save(document));
    }

    @Transactional
    public void deleteDocument(UUID userId, UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        User user = userRepository.findById(userId).orElseThrow();

        if (!document.getOwner().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to delete this document");
        }

        storageService.delete(document.getStoragePath());
        documentRepository.delete(document);
        
        auditLogService.logAction(AuditAction.DOCUMENT_DELETE, userId, documentId, "Deleted document: " + document.getFileName());
    }

    @Transactional
    public DocumentResponse reprocessDocument(UUID userId, UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        User user = userRepository.findById(userId).orElseThrow();

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.EMPLOYEE) {
            throw new AccessDeniedException("Only ADMIN and EMPLOYEE can reprocess documents");
        }

        // Delete existing chunks
        documentChunkRepository.deleteByDocumentId(documentId);

        // Reset status
        document.setStatus(com.nilesh.knowledgebase.entity.DocumentStatus.UPLOADED);
        document.setErrorMessage(null);
        Document savedDocument = documentRepository.save(document);

        final UUID savedId = savedDocument.getId();
        final String fileName = document.getFileName();

        log.info("[Reprocess] REGISTERING_AFTER_COMMIT  documentId={}", savedId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("[Reprocess] AFTER_COMMIT_FIRED  documentId={}", savedId);
                auditLogService.logAction(AuditAction.DOCUMENT_REPROCESSED, userId, savedId,
                        "Reprocessed document: " + fileName);
                log.info("[Reprocess] TRIGGERING_ASYNC_PROCESSING  documentId={}", savedId);
                documentProcessingService.processDocumentAsync(savedId);
            }
        });

        return documentMapper.toResponse(savedDocument);
    }
}

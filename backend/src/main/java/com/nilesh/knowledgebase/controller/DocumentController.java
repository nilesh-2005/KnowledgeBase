package com.nilesh.knowledgebase.controller;


import com.nilesh.knowledgebase.dto.DocumentResponse;
import com.nilesh.knowledgebase.dto.DocumentUploadRequest;
import com.nilesh.knowledgebase.entity.Visibility;
import com.nilesh.knowledgebase.security.UserPrincipal;
import com.nilesh.knowledgebase.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestPart("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("visibility") Visibility visibility,
            @RequestParam(value = "collectionId", required = false) UUID collectionId,
            @RequestParam(value = "tagIds", required = false) Set<UUID> tagIds) {
        
        DocumentUploadRequest request = new DocumentUploadRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setVisibility(visibility);
        request.setCollectionId(collectionId);
        request.setTagIds(tagIds);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(user.getId(), file, request));
    }

    @GetMapping
    public ResponseEntity<Page<DocumentResponse>> getVisibleDocuments(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(documentService.getVisibleDocuments(user.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DocumentResponse>> searchDocuments(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(documentService.searchDocuments(user.getId(), query, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getDocumentById(user.getId(), id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id) {
        
        DocumentResponse doc = documentService.getDocumentById(user.getId(), id);
        Resource resource = documentService.downloadDocumentResource(user.getId(), id);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(doc.getFileType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<DocumentResponse> updateDocument(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id,
            @Valid @RequestBody DocumentUploadRequest request) {
        return ResponseEntity.ok(documentService.updateDocument(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Void> deleteDocument(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id) {
        documentService.deleteDocument(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reprocess")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<DocumentResponse> reprocessDocument(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id) {
        return ResponseEntity.ok(documentService.reprocessDocument(user.getId(), id));
    }
}

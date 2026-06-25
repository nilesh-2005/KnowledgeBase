package com.nilesh.knowledgebase.controller;

import com.nilesh.knowledgebase.dto.CollectionDto;
import com.nilesh.knowledgebase.security.UserPrincipal;
import com.nilesh.knowledgebase.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * Returns ALL workspace collections — visible to every authenticated user.
     * Collections in a knowledge base are workspace-wide resources, not personal.
     */
    @GetMapping
    public ResponseEntity<Page<CollectionDto>> getAllCollections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(collectionService.getAllCollections(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionDto> getCollectionById(@PathVariable UUID id) {
        return ResponseEntity.ok(collectionService.getCollectionById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<CollectionDto> createCollection(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CollectionDto collectionDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionService.createCollection(user.getId(), collectionDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<CollectionDto> updateCollection(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id,
            @Valid @RequestBody CollectionDto collectionDto) {
        return ResponseEntity.ok(collectionService.updateCollection(user.getId(), id, collectionDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Void> deleteCollection(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id) {
        collectionService.deleteCollection(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}

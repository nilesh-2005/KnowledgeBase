package com.nilesh.knowledgebase.controller;

import com.nilesh.knowledgebase.dto.TagDto;
import com.nilesh.knowledgebase.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDto> getTagById(@PathVariable UUID id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<TagDto> createTag(@Valid @RequestBody TagDto tagDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(tagDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<TagDto> updateTag(@PathVariable UUID id, @Valid @RequestBody TagDto tagDto) {
        return ResponseEntity.ok(tagService.updateTag(id, tagDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}

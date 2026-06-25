package com.nilesh.knowledgebase.controller;

import com.nilesh.knowledgebase.dto.AuditLogResponse;
import com.nilesh.knowledgebase.entity.AuditAction;
import com.nilesh.knowledgebase.entity.AuditLog;
import com.nilesh.knowledgebase.repository.AuditLogRepository;
import com.nilesh.knowledgebase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AuditLog> logs;

        if (action != null && actorId != null) {
            logs = auditLogRepository.findByActionAndActorId(action, actorId, pageRequest);
        } else if (action != null) {
            logs = auditLogRepository.findByAction(action, pageRequest);
        } else if (actorId != null) {
            logs = auditLogRepository.findByActorId(actorId, pageRequest);
        } else {
            logs = auditLogRepository.findAll(pageRequest);
        }

        return ResponseEntity.ok(logs.map(this::toResponse));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        String actorEmail = userRepository.findById(log.getActorId())
                .map(u -> u.getEmail())
                .orElse("deleted-user");

        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .actorId(log.getActorId())
                .actorEmail(actorEmail)
                .targetId(log.getTargetId())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}

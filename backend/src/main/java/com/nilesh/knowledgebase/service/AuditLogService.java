package com.nilesh.knowledgebase.service;

import com.nilesh.knowledgebase.entity.AuditAction;
import com.nilesh.knowledgebase.entity.AuditLog;
import com.nilesh.knowledgebase.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an audit event asynchronously to prevent blocking the main business logic.
     */
    @Async
    public void logAction(AuditAction action, UUID actorId, UUID targetId, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .actorId(actorId)
                .targetId(targetId)
                .details(details)
                .build();
        
        auditLogRepository.save(log);
    }
}

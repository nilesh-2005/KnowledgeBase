package com.nilesh.knowledgebase.dto;

import com.nilesh.knowledgebase.entity.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private UUID id;
    private AuditAction action;
    private UUID actorId;
    private String actorEmail;
    private UUID targetId;
    private String details;
    private Instant createdAt;
}

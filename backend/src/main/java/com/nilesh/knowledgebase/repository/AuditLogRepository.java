package com.nilesh.knowledgebase.repository;

import com.nilesh.knowledgebase.entity.AuditAction;
import com.nilesh.knowledgebase.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByActorId(UUID actorId, Pageable pageable);

    Page<AuditLog> findByActionAndActorId(AuditAction action, UUID actorId, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetween(Instant from, Instant to, Pageable pageable);

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

package com.nilesh.knowledgebase.repository;

import com.nilesh.knowledgebase.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Page<Conversation> findByUserIdAndArchivedFalseAndDeletedAtIsNullOrderByUpdatedAtDesc(UUID userId, Pageable pageable);
}

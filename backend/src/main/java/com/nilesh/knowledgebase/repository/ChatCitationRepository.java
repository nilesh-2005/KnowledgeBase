package com.nilesh.knowledgebase.repository;

import com.nilesh.knowledgebase.entity.ChatCitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatCitationRepository extends JpaRepository<ChatCitation, UUID> {
}

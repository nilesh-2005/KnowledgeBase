package com.nilesh.knowledgebase.repository;

import com.nilesh.knowledgebase.entity.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    Page<Collection> findByOwnerId(UUID ownerId, Pageable pageable);
}

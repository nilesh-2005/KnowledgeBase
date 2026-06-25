package com.nilesh.knowledgebase.repository;

import com.nilesh.knowledgebase.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Page<Document> findByOwnerId(UUID ownerId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE " +
           "(d.visibility = com.nilesh.knowledgebase.entity.Visibility.PUBLIC) OR " +
           "(d.visibility = com.nilesh.knowledgebase.entity.Visibility.TEAM) OR " +
           "(d.owner.id = :userId)")
    Page<Document> findVisibleDocuments(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT DISTINCT d FROM Document d LEFT JOIN d.tags t WHERE " +
           "((d.visibility = com.nilesh.knowledgebase.entity.Visibility.PUBLIC) OR (d.visibility = com.nilesh.knowledgebase.entity.Visibility.TEAM) OR (d.owner.id = :userId)) AND " +
           "((LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           " (LOWER(d.description) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           " (LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))))")
    Page<Document> searchDocuments(@Param("userId") UUID userId, @Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT d FROM Document d LEFT JOIN d.tags t WHERE " +
           "((LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           " (LOWER(d.description) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           " (LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))))")
    Page<Document> searchAllDocuments(@Param("query") String query, Pageable pageable);

    Page<Document> findByCollectionId(UUID collectionId, Pageable pageable);
}

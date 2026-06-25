package com.nilesh.knowledgebase.repository;

import com.nilesh.knowledgebase.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {
    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(UUID documentId);
    long countByDocumentId(UUID documentId);
    void deleteByDocumentId(UUID documentId);

    @Query("""
            SELECT c FROM DocumentChunk c
            JOIN c.document d
            WHERE c.embedding IS NOT NULL
            AND (d.visibility = com.nilesh.knowledgebase.entity.Visibility.PUBLIC
                 OR d.visibility = com.nilesh.knowledgebase.entity.Visibility.TEAM
                 OR d.owner.id = :userId)
            ORDER BY l2_distance(c.embedding, cast(:embedding as vector))
            """)
    List<DocumentChunk> findSimilarChunksForUser(@Param("embedding") float[] embedding,
                                                   @Param("userId") UUID userId,
                                                   Pageable pageable);

    @Query("""
            SELECT c FROM DocumentChunk c
            WHERE c.embedding IS NOT NULL
            ORDER BY l2_distance(c.embedding, cast(:embedding as vector))
            """)
    List<DocumentChunk> findSimilarChunksForAdmin(@Param("embedding") float[] embedding, Pageable pageable);

    @Query("""
            SELECT c, l2_distance(c.embedding, cast(:embedding as vector))
            FROM DocumentChunk c
            JOIN c.document d
            WHERE c.embedding IS NOT NULL
            AND (d.visibility = com.nilesh.knowledgebase.entity.Visibility.PUBLIC
                 OR d.visibility = com.nilesh.knowledgebase.entity.Visibility.TEAM
                 OR d.owner.id = :userId)
            ORDER BY l2_distance(c.embedding, cast(:embedding as vector))
            """)
    List<Object[]> findSimilarChunksWithDistanceForUser(@Param("embedding") float[] embedding,
                                                        @Param("userId") UUID userId,
                                                        Pageable pageable);

    @Query("""
            SELECT c, l2_distance(c.embedding, cast(:embedding as vector))
            FROM DocumentChunk c
            WHERE c.embedding IS NOT NULL
            ORDER BY l2_distance(c.embedding, cast(:embedding as vector))
            """)
    List<Object[]> findSimilarChunksWithDistanceForAdmin(@Param("embedding") float[] embedding,
                                                         Pageable pageable);
}

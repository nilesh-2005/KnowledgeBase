package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.dto.RetrievedChunk;
import com.nilesh.knowledgebase.dto.SemanticSearchResult;
import com.nilesh.knowledgebase.entity.DocumentChunk;

import java.util.List;
import java.util.UUID;

/**
 * Foundational interface for executing semantic similarity searches against the vector database.
 */
public interface UnifiedSearchService {

    List<DocumentChunk> searchSimilar(UUID userId, String query, int topK, String mode);

    List<SemanticSearchResult> searchWithScores(UUID userId, String query, int topK, String mode);

    List<RetrievedChunk> searchForUser(UUID userId, String query, int topK, String mode);
}

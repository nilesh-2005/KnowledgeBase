package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.dto.RetrievedChunk;
import com.nilesh.knowledgebase.dto.SemanticSearchResult;
import com.nilesh.knowledgebase.entity.DocumentChunk;

import java.util.List;
import java.util.UUID;

/**
 * Foundational interface for executing semantic similarity searches against the vector database.
 */
public interface SimilaritySearchService {

    /**
     * Searches for document chunks visible to the user that are semantically similar to the query.
     */
    List<DocumentChunk> searchSimilar(UUID userId, String query, int topK);

    /**
     * Searches for semantically similar visible chunks and returns relevance scores.
     */
    List<SemanticSearchResult> searchWithScores(UUID userId, String query, int topK);

    /**
     * User-scoped retrieval for RAG with score filtering, deduplication, and rich metadata.
     */
    List<RetrievedChunk> searchForUser(UUID userId, String query, int topK);
}

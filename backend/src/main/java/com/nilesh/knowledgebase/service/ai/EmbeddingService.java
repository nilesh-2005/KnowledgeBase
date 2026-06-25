package com.nilesh.knowledgebase.service.ai;

import java.util.List;

/**
 * Foundational interface for generating vector embeddings from text.
 * To be implemented in future phases (e.g., using Spring AI or OpenAI direct API).
 */
public interface EmbeddingService {
    
    /**
     * Generates a vector embedding for the given text.
     * 
     * @param text The text to embed.
     * @return The vector representation (e.g., 1536 dimensions).
     */
    float[] generateEmbedding(String text);
    
}

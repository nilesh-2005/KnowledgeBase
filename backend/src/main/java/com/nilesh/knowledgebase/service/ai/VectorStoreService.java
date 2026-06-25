package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.entity.DocumentChunk;
import java.util.List;

/**
 * Foundational interface for storing embeddings in the vector database.
 */
public interface VectorStoreService {
    
    /**
     * Saves the generated embeddings for a list of document chunks.
     * 
     * @param chunks The chunks to persist in the vector store.
     */
    void saveEmbeddings(List<DocumentChunk> chunks);
    
}

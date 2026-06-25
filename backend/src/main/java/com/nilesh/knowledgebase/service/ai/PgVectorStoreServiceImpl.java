package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.entity.DocumentChunk;
import com.nilesh.knowledgebase.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PgVectorStoreServiceImpl implements VectorStoreService {

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;

    public PgVectorStoreServiceImpl(EmbeddingService embeddingService, DocumentChunkRepository documentChunkRepository) {
        this.embeddingService = embeddingService;
        this.documentChunkRepository = documentChunkRepository;
    }

    @Override
    @Transactional
    public void saveEmbeddings(List<DocumentChunk> chunks) {
        for (DocumentChunk chunk : chunks) {
            float[] embedding = embeddingService.generateEmbedding(chunk.getContent());
            chunk.setEmbedding(embedding);
        }
        documentChunkRepository.saveAll(chunks);
    }
}

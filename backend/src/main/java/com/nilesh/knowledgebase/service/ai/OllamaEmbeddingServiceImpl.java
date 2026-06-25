package com.nilesh.knowledgebase.service.ai;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class OllamaEmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public OllamaEmbeddingServiceImpl(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public float[] generateEmbedding(String text) {
        return embeddingModel.embed(text);
    }
}

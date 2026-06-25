package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.config.RagProperties;
import com.nilesh.knowledgebase.dto.RetrievedChunk;
import com.nilesh.knowledgebase.dto.SemanticSearchResult;
import com.nilesh.knowledgebase.entity.DocumentChunk;
import com.nilesh.knowledgebase.entity.Role;
import com.nilesh.knowledgebase.entity.User;
import com.nilesh.knowledgebase.exception.ResourceNotFoundException;
import com.nilesh.knowledgebase.repository.DocumentChunkRepository;
import com.nilesh.knowledgebase.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PgSimilaritySearchServiceImpl implements SimilaritySearchService {

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;
    private final UserRepository userRepository;
    private final RagProperties ragProperties;

    public PgSimilaritySearchServiceImpl(EmbeddingService embeddingService,
                                         DocumentChunkRepository documentChunkRepository,
                                         UserRepository userRepository,
                                         RagProperties ragProperties) {
        this.embeddingService = embeddingService;
        this.documentChunkRepository = documentChunkRepository;
        this.userRepository = userRepository;
        this.ragProperties = ragProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentChunk> searchSimilar(UUID userId, String query, int topK) {
        return searchForUser(userId, query, topK).stream()
                .map(this::toDocumentChunkReference)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SemanticSearchResult> searchWithScores(UUID userId, String query, int topK) {
        return searchForUser(userId, query, topK).stream()
                .map(chunk -> new SemanticSearchResult(
                        chunk.documentTitle(),
                        chunk.chunkIndex(),
                        chunk.score(),
                        chunk.content()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetrievedChunk> searchForUser(UUID userId, String query, int topK) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        float[] queryEmbedding = embeddingService.generateEmbedding(query);
        int fetchLimit = topK * ragProperties.getFetchMultiplier();
        PageRequest pageRequest = PageRequest.of(0, fetchLimit);

        List<Object[]> rows = user.getRole() == Role.ADMIN
                ? documentChunkRepository.findSimilarChunksWithDistanceForAdmin(queryEmbedding, pageRequest)
                : documentChunkRepository.findSimilarChunksWithDistanceForUser(queryEmbedding, userId, pageRequest);

        return postProcessRows(rows, topK);
    }

    List<RetrievedChunk> postProcessRows(List<Object[]> rows, int topK) {
        Map<String, RetrievedChunk> deduplicated = new LinkedHashMap<>();

        for (Object[] row : rows) {
            DocumentChunk chunk = (DocumentChunk) row[0];
            double distance = ((Number) row[1]).doubleValue();
            double score = computeScore(distance);

            if (score < ragProperties.getScoreThreshold()) {
                continue;
            }

            String key = chunk.getDocument().getId() + ":" + chunk.getChunkIndex();
            deduplicated.putIfAbsent(key, toRetrievedChunk(chunk, score, distance));

            if (deduplicated.size() >= topK) {
                break;
            }
        }

        return new ArrayList<>(deduplicated.values());
    }

    static double computeScore(double distance) {
        return Math.round((1.0 / (1.0 + distance)) * 100.0) / 100.0;
    }

    private RetrievedChunk toRetrievedChunk(DocumentChunk chunk, double score, double distance) {
        return new RetrievedChunk(
                chunk.getId(),
                chunk.getDocument().getId(),
                chunk.getDocument().getTitle(),
                chunk.getChunkIndex(),
                chunk.getContent(),
                score,
                distance
        );
    }

    private DocumentChunk toDocumentChunkReference(RetrievedChunk chunk) {
        DocumentChunk reference = new DocumentChunk();
        reference.setId(chunk.chunkId());
        reference.setChunkIndex(chunk.chunkIndex());
        reference.setContent(chunk.content());
        return reference;
    }
}

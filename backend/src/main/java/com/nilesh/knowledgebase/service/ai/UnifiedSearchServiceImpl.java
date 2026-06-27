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
public class UnifiedSearchServiceImpl implements UnifiedSearchService {

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;
    private final UserRepository userRepository;
    private final RagProperties ragProperties;

    public UnifiedSearchServiceImpl(EmbeddingService embeddingService,
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
    public List<DocumentChunk> searchSimilar(UUID userId, String query, int topK, String mode) {
        return searchForUser(userId, query, topK, mode).stream()
                .map(this::toDocumentChunkReference)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SemanticSearchResult> searchWithScores(UUID userId, String query, int topK, String mode) {
        return searchForUser(userId, query, topK, mode).stream()
                .map(chunk -> new SemanticSearchResult(
                        chunk.documentId(),
                        chunk.documentTitle(),
                        chunk.chunkIndex(),
                        chunk.score(),
                        chunk.content(),
                        chunk.mode(),
                        chunk.semanticScore(),
                        chunk.keywordScore()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetrievedChunk> searchForUser(UUID userId, String query, int topK, String mode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if ("keyword".equalsIgnoreCase(mode)) {
            return performKeywordSearch(user, query, topK);
        } else if ("semantic".equalsIgnoreCase(mode)) {
            return performSemanticSearch(user, query, topK);
        } else {
            return performHybridSearch(user, query, topK);
        }
    }

    private List<RetrievedChunk> performSemanticSearch(User user, String query, int topK) {
        float[] queryEmbedding = embeddingService.generateEmbedding(query);
        int fetchLimit = topK * ragProperties.getFetchMultiplier();
        PageRequest pageRequest = PageRequest.of(0, fetchLimit);

        List<Object[]> rows = user.getRole() == Role.ADMIN
                ? documentChunkRepository.findSimilarChunksWithDistanceForAdmin(queryEmbedding, pageRequest)
                : documentChunkRepository.findSimilarChunksWithDistanceForUser(queryEmbedding, user.getId(), pageRequest);

        return processSemanticRows(rows, topK);
    }

    private List<RetrievedChunk> performKeywordSearch(User user, String query, int topK) {
        // Postgres native tsquery uses & and | instead of spaces for AND/OR.
        // For a simple phrase query handling, we replace spaces with &
        String formattedQuery = query.trim().replaceAll("\\s+", " & ");
        if (formattedQuery.isEmpty()) {
            return new ArrayList<>();
        }

        int fetchLimit = topK * ragProperties.getFetchMultiplier();
        PageRequest pageRequest = PageRequest.of(0, fetchLimit);

        List<Object[]> rows;
        try {
            rows = user.getRole() == Role.ADMIN
                ? documentChunkRepository.findKeywordChunksForAdmin(formattedQuery, pageRequest)
                : documentChunkRepository.findKeywordChunksForUser(formattedQuery, user.getId(), pageRequest);
        } catch (Exception e) {
            // fallback if tsquery parsing fails
            return new ArrayList<>();
        }

        return processKeywordRows(rows, topK);
    }

    private List<RetrievedChunk> performHybridSearch(User user, String query, int topK) {
        // fetch more for hybrid to allow good merging
        int hybridTopK = topK * 2;
        List<RetrievedChunk> semanticResults = performSemanticSearch(user, query, hybridTopK);
        List<RetrievedChunk> keywordResults = performKeywordSearch(user, query, hybridTopK);

        Map<String, RetrievedChunk> merged = new LinkedHashMap<>();

        // Add semantic results
        for (RetrievedChunk chunk : semanticResults) {
            merged.put(chunk.chunkId().toString(), chunk);
        }

        // Add or update with keyword results
        for (RetrievedChunk kwChunk : keywordResults) {
            String id = kwChunk.chunkId().toString();
            if (merged.containsKey(id)) {
                RetrievedChunk semChunk = merged.get(id);
                double combinedScore = calculateHybridScore(semChunk.semanticScore(), kwChunk.keywordScore());
                merged.put(id, new RetrievedChunk(
                        semChunk.chunkId(), semChunk.documentId(), semChunk.documentTitle(),
                        semChunk.chunkIndex(), semChunk.content(), combinedScore, semChunk.distance(),
                        "hybrid", semChunk.semanticScore(), kwChunk.keywordScore()
                ));
            } else {
                double combinedScore = calculateHybridScore(0.0, kwChunk.keywordScore());
                merged.put(id, new RetrievedChunk(
                        kwChunk.chunkId(), kwChunk.documentId(), kwChunk.documentTitle(),
                        kwChunk.chunkIndex(), kwChunk.content(), combinedScore, kwChunk.distance(),
                        "hybrid", 0.0, kwChunk.keywordScore()
                ));
            }
        }

        // Update mode for semantic-only chunks in hybrid mode
        List<RetrievedChunk> finalResults = new ArrayList<>();
        for (RetrievedChunk chunk : merged.values()) {
            if ("semantic".equals(chunk.mode())) {
                double combinedScore = calculateHybridScore(chunk.semanticScore(), 0.0);
                finalResults.add(new RetrievedChunk(
                        chunk.chunkId(), chunk.documentId(), chunk.documentTitle(),
                        chunk.chunkIndex(), chunk.content(), combinedScore, chunk.distance(),
                        "hybrid", chunk.semanticScore(), 0.0
                ));
            } else {
                finalResults.add(chunk);
            }
        }

        finalResults.sort((a, b) -> Double.compare(b.score(), a.score()));

        return finalResults.stream().limit(topK).toList();
    }

    private double calculateHybridScore(double semanticScore, double keywordScore) {
        return (semanticScore * ragProperties.getSemanticWeight()) + (keywordScore * ragProperties.getKeywordWeight());
    }

    private List<RetrievedChunk> processSemanticRows(List<Object[]> rows, int topK) {
        Map<String, RetrievedChunk> deduplicated = new LinkedHashMap<>();
        for (Object[] row : rows) {
            DocumentChunk chunk = (DocumentChunk) row[0];
            double distance = ((Number) row[1]).doubleValue();
            double score = computeScore(distance);

            if (score < ragProperties.getScoreThreshold()) {
                continue;
            }

            String key = chunk.getDocument().getId() + ":" + chunk.getChunkIndex();
            if (!deduplicated.containsKey(key)) {
                deduplicated.put(key, new RetrievedChunk(
                        chunk.getId(), chunk.getDocument().getId(), chunk.getDocument().getTitle(),
                        chunk.getChunkIndex(), chunk.getContent(), score, distance,
                        "semantic", score, 0.0
                ));
            }
            if (deduplicated.size() >= topK) break;
        }
        return new ArrayList<>(deduplicated.values());
    }

    private List<RetrievedChunk> processKeywordRows(List<Object[]> rows, int topK) {
        Map<String, RetrievedChunk> deduplicated = new LinkedHashMap<>();
        for (Object[] row : rows) {
            // SELECT c.id, d.id, d.title, c.chunk_index, c.content, ts_rank(...) as rank
            UUID chunkId = (UUID) row[0];
            UUID docId = (UUID) row[1];
            String docTitle = (String) row[2];
            int chunkIndex = (Integer) row[3];
            String content = (String) row[4];
            double rank = ((Number) row[5]).doubleValue();

            // Normalize ts_rank to [0,1] approximately if we can, or just use as is for now
            // ts_rank typically returns 0.0 to 1.0 but can go higher. Let's cap at 1.0.
            double score = Math.min(rank, 1.0);

            String key = docId + ":" + chunkIndex;
            if (!deduplicated.containsKey(key)) {
                deduplicated.put(key, new RetrievedChunk(
                        chunkId, docId, docTitle, chunkIndex, content, score, 0.0,
                        "keyword", 0.0, score
                ));
            }
            if (deduplicated.size() >= topK) break;
        }
        return new ArrayList<>(deduplicated.values());
    }

    static double computeScore(double distance) {
        return Math.round((1.0 / (1.0 + distance)) * 100.0) / 100.0;
    }

    private DocumentChunk toDocumentChunkReference(RetrievedChunk chunk) {
        DocumentChunk reference = new DocumentChunk();
        reference.setId(chunk.chunkId());
        reference.setChunkIndex(chunk.chunkIndex());
        reference.setContent(chunk.content());
        return reference;
    }
}

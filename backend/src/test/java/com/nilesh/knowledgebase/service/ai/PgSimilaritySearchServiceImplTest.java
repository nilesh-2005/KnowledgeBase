package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.config.RagProperties;
import com.nilesh.knowledgebase.dto.RetrievedChunk;
import com.nilesh.knowledgebase.entity.Document;
import com.nilesh.knowledgebase.entity.DocumentChunk;
import com.nilesh.knowledgebase.entity.Role;
import com.nilesh.knowledgebase.entity.User;
import com.nilesh.knowledgebase.entity.Visibility;
import com.nilesh.knowledgebase.repository.DocumentChunkRepository;
import com.nilesh.knowledgebase.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PgSimilaritySearchServiceImplTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private DocumentChunkRepository documentChunkRepository;

    @Mock
    private UserRepository userRepository;

    private RagProperties ragProperties;
    private PgSimilaritySearchServiceImpl similaritySearchService;

    @BeforeEach
    void setUp() {
        ragProperties = new RagProperties();
        ragProperties.setScoreThreshold(0.35);
        ragProperties.setFetchMultiplier(2);
        similaritySearchService = new PgSimilaritySearchServiceImpl(
                embeddingService,
                documentChunkRepository,
                userRepository,
                ragProperties
        );
    }

    @Test
    void computeScoreUsesInverseDistanceFormula() {
        assertEquals(0.5, PgSimilaritySearchServiceImpl.computeScore(1.0));
        assertEquals(0.91, PgSimilaritySearchServiceImpl.computeScore(0.1));
    }

    @Test
    void postProcessFiltersBelowThresholdAndDedupes() {
        UUID documentId = UUID.randomUUID();
        List<Object[]> rows = List.of(
                row(documentId, 1, "visible chunk", 0.1),
                row(documentId, 1, "duplicate chunk", 0.1),
                row(documentId, 2, "low score chunk", 5.0)
        );

        List<RetrievedChunk> results = similaritySearchService.postProcessRows(rows, 5);

        assertEquals(1, results.size());
        assertEquals(1, results.get(0).chunkIndex());
        assertTrue(results.get(0).score() >= 0.35);
    }

    @Test
    void searchForUserUsesAdminQueryForAdminUsers() {
        UUID userId = UUID.randomUUID();
        User admin = User.builder().id(userId).role(Role.ADMIN).build();
        float[] embedding = new float[] {0.1f, 0.2f};

        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));
        when(embeddingService.generateEmbedding("test")).thenReturn(embedding);
        when(documentChunkRepository.findSimilarChunksWithDistanceForAdmin(eq(embedding), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(row(UUID.randomUUID(), 0, "admin visible", 0.2)));

        List<RetrievedChunk> results = similaritySearchService.searchForUser(userId, "test", 3);

        verify(documentChunkRepository).findSimilarChunksWithDistanceForAdmin(eq(embedding), any(Pageable.class));
        assertEquals(1, results.size());
    }

    @Test
    void searchForUserUsesVisibilityQueryForNonAdminUsers() {
        UUID userId = UUID.randomUUID();
        User viewer = User.builder().id(userId).role(Role.VIEWER).build();
        float[] embedding = new float[] {0.1f, 0.2f};

        when(userRepository.findById(userId)).thenReturn(Optional.of(viewer));
        when(embeddingService.generateEmbedding("test")).thenReturn(embedding);
        when(documentChunkRepository.findSimilarChunksWithDistanceForUser(eq(embedding), eq(userId), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(row(UUID.randomUUID(), 0, "viewer visible", 0.2)));

        List<RetrievedChunk> results = similaritySearchService.searchForUser(userId, "test", 3);

        verify(documentChunkRepository).findSimilarChunksWithDistanceForUser(eq(embedding), eq(userId), any(Pageable.class));
        assertEquals(1, results.size());
    }

    private Object[] row(UUID documentId, int chunkIndex, String content, double distance) {
        Document document = Document.builder()
                .id(documentId)
                .title("Doc " + chunkIndex)
                .visibility(Visibility.PRIVATE)
                .build();
        DocumentChunk chunk = DocumentChunk.builder()
                .id(UUID.randomUUID())
                .document(document)
                .chunkIndex(chunkIndex)
                .content(content)
                .build();
        return new Object[] {chunk, distance};
    }
}

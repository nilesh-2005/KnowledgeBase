package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.config.RagProperties;
import com.nilesh.knowledgebase.dto.ChatResponse;
import com.nilesh.knowledgebase.dto.ChatSource;
import com.nilesh.knowledgebase.dto.RetrievedChunk;
import com.nilesh.knowledgebase.repository.DocumentRepository;
import com.nilesh.knowledgebase.service.ChatHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagServiceTest {

    private UnifiedSearchService unifiedSearchService;
    private PromptBuilderService promptBuilderService;
    private LlmService llmService;
    private RagProperties ragProperties;
    private ChatHistoryService chatHistoryService;
    private DocumentRepository documentRepository;
    private RagService ragService;

    @BeforeEach
    void setUp() {
        unifiedSearchService = mock(UnifiedSearchService.class);
        promptBuilderService = mock(PromptBuilderService.class);
        llmService = mock(LlmService.class);
        ragProperties = new RagProperties();
        ragProperties.setTopK(5);
        ragProperties.setExcerptLength(200);
        chatHistoryService = mock(ChatHistoryService.class);
        documentRepository = mock(DocumentRepository.class);
        ragService = new RagService(unifiedSearchService, promptBuilderService, llmService, ragProperties, chatHistoryService, documentRepository);
    }

    @Test
    void askReturnsRefusalWhenNoRetrievalResults() {
        UUID userId = UUID.randomUUID();
        when(unifiedSearchService.searchForUser(userId, "unknown topic", 5, "hybrid")).thenReturn(List.of());

        ChatResponse response = ragService.ask(userId, "unknown topic", 5);

        assertEquals("I don't have enough information in the knowledge base to answer that.", response.answer());
        assertTrue(response.sources().isEmpty());
        assertEquals("none", response.confidence());
        assertEquals(0, response.retrievalCount());
    }

    @Test
    void askBuildsSourcesAndCallsLlmWhenContextExists() {
        UUID userId = UUID.randomUUID();
        RetrievedChunk chunk = new RetrievedChunk(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Dark Psychology",
                105,
                "Psychological manipulation uses influence tactics.",
                0.82,
                0.22,
                "hybrid",
                0.82,
                0.22
        );
        when(unifiedSearchService.searchForUser(userId, "manipulation", 5, "hybrid")).thenReturn(List.of(chunk));
        when(promptBuilderService.selectChunksForContext(List.of(chunk))).thenReturn(List.of(chunk));
        when(promptBuilderService.buildContext(List.of(chunk))).thenReturn("[SOURCE 1] context");
        when(promptBuilderService.buildUserPrompt(eq("manipulation"), eq("[SOURCE 1] context")))
                .thenReturn("user prompt");
        when(llmService.generate(anyString(), eq("user prompt")))
                .thenReturn("Manipulation is influence without consent. [SOURCE 1]");

        ChatResponse response = ragService.ask(userId, "manipulation", 5);

        verify(llmService).generate(anyString(), eq("user prompt"));
        assertEquals("Manipulation is influence without consent. [SOURCE 1]", response.answer());
        assertEquals("high", response.confidence());
        assertEquals(1, response.retrievalCount());
        assertEquals(1, response.sources().size());
        ChatSource source = response.sources().get(0);
        assertEquals(1, source.sourceIndex());
        assertEquals("Dark Psychology", source.documentTitle());
        assertEquals(105, source.chunkIndex());
        assertEquals(0.82, source.score());
    }

    @Test
    void askStripsSourcesWhenLlmRefusesDespiteWeakRetrieval() {
        UUID userId = UUID.randomUUID();
        RetrievedChunk chunk = new RetrievedChunk(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Unrelated Doc",
                0,
                "Some unrelated content.",
                0.47,
                1.13,
                "hybrid",
                0.47,
                1.13
        );
        when(unifiedSearchService.searchForUser(userId, "capital of Mars", 5, "hybrid")).thenReturn(List.of(chunk));
        when(promptBuilderService.selectChunksForContext(List.of(chunk))).thenReturn(List.of(chunk));
        when(promptBuilderService.buildContext(List.of(chunk))).thenReturn("[SOURCE 1] context");
        when(promptBuilderService.buildUserPrompt(eq("capital of Mars"), eq("[SOURCE 1] context")))
                .thenReturn("user prompt");
        when(llmService.generate(anyString(), eq("user prompt")))
                .thenReturn("I don't have enough information in the knowledge base to answer that.");

        ChatResponse response = ragService.ask(userId, "capital of Mars", 5);

        assertEquals("I don't have enough information in the knowledge base to answer that.", response.answer());
        assertTrue(response.sources().isEmpty());
        assertEquals("none", response.confidence());
        assertEquals(0, response.retrievalCount());
    }

    @Test
    void askReturnsRefusalWhenTopScoreBelowMinimumConfidenceThreshold() {
        UUID userId = UUID.randomUUID();
        RetrievedChunk chunk = new RetrievedChunk(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Weak Match",
                0,
                "Loosely related content.",
                0.40,
                1.5,
                "hybrid",
                0.40,
                1.5
        );
        when(unifiedSearchService.searchForUser(userId, "obscure topic", 5, "hybrid")).thenReturn(List.of(chunk));

        ChatResponse response = ragService.ask(userId, "obscure topic", 5);

        assertEquals("I don't have enough information in the knowledge base to answer that.", response.answer());
        assertTrue(response.sources().isEmpty());
        assertEquals("none", response.confidence());
        assertEquals(0, response.retrievalCount());
    }

    @Test
    void askReturnsLowConfidenceForMarginalRetrieval() {
        UUID userId = UUID.randomUUID();
        RetrievedChunk chunk = new RetrievedChunk(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Partial Match",
                2,
                "Somewhat relevant content.",
                0.52,
                0.92,
                "hybrid",
                0.52,
                0.92
        );
        when(unifiedSearchService.searchForUser(userId, "partial", 5, "hybrid")).thenReturn(List.of(chunk));
        when(promptBuilderService.selectChunksForContext(List.of(chunk))).thenReturn(List.of(chunk));
        when(promptBuilderService.buildContext(List.of(chunk))).thenReturn("[SOURCE 1] context");
        when(promptBuilderService.buildUserPrompt(eq("partial"), eq("[SOURCE 1] context")))
                .thenReturn("user prompt");
        when(llmService.generate(anyString(), eq("user prompt")))
                .thenReturn("A partial answer. [SOURCE 1]");

        ChatResponse response = ragService.ask(userId, "partial", 5);

        assertEquals("low", response.confidence());
        assertEquals(1, response.retrievalCount());
        assertEquals(1, response.sources().size());
    }
}

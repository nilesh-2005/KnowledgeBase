package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.config.RagProperties;
import com.nilesh.knowledgebase.dto.RetrievedChunk;
import org.junit.jupiter.api.Test;
import com.nilesh.knowledgebase.repository.DocumentRepository;
import com.nilesh.knowledgebase.service.ChatHistoryService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagServicePromptTest {

    @Test
    void askIncludesRetrievedChunksInLlmPrompt() {
        UnifiedSearchService unifiedSearchService = mock(UnifiedSearchService.class);
        LlmService llmService = mock(LlmService.class);
        RagProperties properties = new RagProperties();
        properties.setTopK(5);
        properties.setExcerptLength(200);
        properties.setMaxContextChars(12000);

        UUID userId = UUID.randomUUID();
        RetrievedChunk chunk = new RetrievedChunk(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Private Notes",
                0,
                "Secret content",
                0.9,
                0.1,
                "hybrid",
                0.9,
                0.1
        );

        when(unifiedSearchService.searchForUser(userId, "secret", 5, "hybrid")).thenReturn(List.of(chunk));
        when(llmService.generate(eq(OllamaChatServiceImpl.defaultSystemPrompt()), org.mockito.ArgumentMatchers.argThat(
                prompt -> prompt.contains("Private Notes") && prompt.contains("Secret content")
        ))).thenReturn("Answer from context [SOURCE 1]");

        ChatHistoryService chatHistoryService = mock(ChatHistoryService.class);
        DocumentRepository documentRepository = mock(DocumentRepository.class);

        RagService ragService = new RagService(
                unifiedSearchService,
                new PromptBuilderService(properties),
                llmService,
                properties,
                chatHistoryService,
                documentRepository
        );

        var response = ragService.ask(userId, "secret", 5);

        verify(llmService).generate(eq(OllamaChatServiceImpl.defaultSystemPrompt()), anyString());
        assertEquals(1, response.sources().size());
        assertEquals(1, response.sources().get(0).sourceIndex());
    }
}

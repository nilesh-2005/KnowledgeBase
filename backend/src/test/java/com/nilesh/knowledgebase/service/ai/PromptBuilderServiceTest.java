package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.config.RagProperties;
import com.nilesh.knowledgebase.dto.RetrievedChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptBuilderServiceTest {

    private PromptBuilderService promptBuilderService;

    @BeforeEach
    void setUp() {
        RagProperties properties = new RagProperties();
        properties.setMaxContextChars(5000);
        promptBuilderService = new PromptBuilderService(properties);
    }

    @Test
    void buildContextNumbersSourcesAndIncludesMetadata() {
        List<RetrievedChunk> chunks = List.of(
                chunk("Dark Psychology", 105, "Manipulation tactics overview"),
                chunk("AI", 1, "Embedding pipeline details")
        );

        String context = promptBuilderService.buildContext(chunks);

        assertTrue(context.contains("[SOURCE 1]"));
        assertTrue(context.contains("Document: Dark Psychology"));
        assertTrue(context.contains("Chunk: 105"));
        assertTrue(context.contains("Manipulation tactics overview"));
        assertTrue(context.contains("[SOURCE 2]"));
        assertTrue(context.contains("Document: AI"));
    }

    @Test
    void buildUserPromptIncludesQuestionAndContext() {
        String prompt = promptBuilderService.buildUserPrompt(
                "What is manipulation?",
                "[SOURCE 1]\nDocument: Dark Psychology\nContent:\nExample\n---"
        );

        assertTrue(prompt.contains("What is manipulation?"));
        assertTrue(prompt.contains("Context:"));
        assertTrue(prompt.contains("Answer using only the provided context."));
    }

    @Test
    void selectChunksForContextRespectsCharacterBudget() {
        RagProperties properties = new RagProperties();
        properties.setMaxContextChars(200);
        PromptBuilderService service = new PromptBuilderService(properties);

        List<RetrievedChunk> chunks = List.of(
                chunk("Doc A", 0, "A".repeat(120)),
                chunk("Doc B", 1, "B".repeat(120))
        );

        List<RetrievedChunk> selected = service.selectChunksForContext(chunks);

        assertEquals(1, selected.size());
        assertEquals("Doc A", selected.get(0).documentTitle());
    }

    private RetrievedChunk chunk(String title, int chunkIndex, String content) {
        return new RetrievedChunk(
                UUID.randomUUID(),
                UUID.randomUUID(),
                title,
                chunkIndex,
                content,
                0.82,
                0.22,
                "hybrid",
                0.82,
                0.22
        );
    }
}

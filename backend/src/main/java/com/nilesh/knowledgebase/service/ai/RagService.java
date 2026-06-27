package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.config.RagProperties;
import com.nilesh.knowledgebase.dto.ChatResponse;
import com.nilesh.knowledgebase.dto.ChatSource;
import com.nilesh.knowledgebase.dto.RetrievedChunk;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RagService {

    private final UnifiedSearchService unifiedSearchService;
    private final PromptBuilderService promptBuilderService;
    private final LlmService llmService;
    private final RagProperties ragProperties;
    private final com.nilesh.knowledgebase.service.ChatHistoryService chatHistoryService;
    private final com.nilesh.knowledgebase.repository.DocumentRepository documentRepository;

    public RagService(UnifiedSearchService unifiedSearchService,
                      PromptBuilderService promptBuilderService,
                      LlmService llmService,
                      RagProperties ragProperties,
                      com.nilesh.knowledgebase.service.ChatHistoryService chatHistoryService,
                      com.nilesh.knowledgebase.repository.DocumentRepository documentRepository) {
        this.unifiedSearchService = unifiedSearchService;
        this.promptBuilderService = promptBuilderService;
        this.llmService = llmService;
        this.ragProperties = ragProperties;
        this.chatHistoryService = chatHistoryService;
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public ChatResponse ask(UUID userId, String question, Integer topK) {
        int effectiveTopK = topK != null ? topK : ragProperties.getTopK();
        List<RetrievedChunk> retrieved = unifiedSearchService.searchForUser(userId, question, effectiveTopK, "hybrid");

        if (!RetrievalConfidence.hasUsableRetrieval(retrieved, ragProperties.getScoreThreshold())) {
            return insufficientContextResponse();
        }

        List<RetrievedChunk> contextChunks = promptBuilderService.selectChunksForContext(retrieved);
        String context = promptBuilderService.buildContext(contextChunks);
        String userPrompt = promptBuilderService.buildUserPrompt(question, context);

        String answer = llmService.generate(OllamaChatServiceImpl.defaultSystemPrompt(), userPrompt);
        if (RetrievalConfidence.isRefusalAnswer(answer)) {
            return insufficientContextResponse();
        }

        double topScore = retrieved.getFirst().score();
        String confidence = RetrievalConfidence.classify(topScore);
        List<ChatSource> sources = buildSources(contextChunks);

        return new ChatResponse(answer, sources, confidence, retrieved.size());
    }

    public reactor.core.publisher.Flux<com.nilesh.knowledgebase.dto.ChatStreamEvent> streamAsk(UUID userId, UUID conversationId, String question, Integer topK) {
        // 1. Save user message synchronously
        com.nilesh.knowledgebase.entity.Conversation conversation = chatHistoryService.getConversation(conversationId, userId);
        com.nilesh.knowledgebase.entity.ChatMessage userMsg = new com.nilesh.knowledgebase.entity.ChatMessage();
        userMsg.setConversation(conversation);
        userMsg.setRole("user");
        userMsg.setContent(question);
        chatHistoryService.saveMessageWithCitations(userMsg, null);

        long startTime = System.currentTimeMillis();
        
        // 2. Retrieval
        int effectiveTopK = topK != null ? topK : ragProperties.getTopK();
        List<RetrievedChunk> retrieved = unifiedSearchService.searchForUser(userId, question, effectiveTopK, "hybrid");
        long retrievalTimeMs = System.currentTimeMillis() - startTime;
        
        if (!RetrievalConfidence.hasUsableRetrieval(retrieved, ragProperties.getScoreThreshold())) {
            com.nilesh.knowledgebase.entity.ChatMessage assistantMsg = new com.nilesh.knowledgebase.entity.ChatMessage();
            assistantMsg.setConversation(conversation);
            assistantMsg.setRole("assistant");
            assistantMsg.setContent(RetrievalConfidence.REFUSAL_ANSWER);
            assistantMsg.setConfidence("none");
            assistantMsg.setRetrievalCount(0);
            chatHistoryService.saveMessageWithCitations(assistantMsg, null);
            
            return reactor.core.publisher.Flux.just(com.nilesh.knowledgebase.dto.ChatStreamEvent.builder()
                .type("ERROR")
                .text(RetrievalConfidence.REFUSAL_ANSWER)
                .build());
        }

        List<RetrievedChunk> contextChunks = promptBuilderService.selectChunksForContext(retrieved);
        String context = promptBuilderService.buildContext(contextChunks);
        String userPrompt = promptBuilderService.buildUserPrompt(question, context);

        double topScore = retrieved.getFirst().score();
        String confidence = RetrievalConfidence.classify(topScore);
        List<ChatSource> sources = buildSources(contextChunks);

        com.nilesh.knowledgebase.dto.ChatStreamEvent metadataEvent = com.nilesh.knowledgebase.dto.ChatStreamEvent.builder()
            .type("METADATA")
            .sources(sources)
            .confidence(confidence)
            .retrievalCount(retrieved.size())
            .retrievalTimeMs(retrievalTimeMs)
            .build();
        
        StringBuilder fullAnswer = new StringBuilder();
        long generationStartTime = System.currentTimeMillis();
        
        reactor.core.publisher.Flux<com.nilesh.knowledgebase.dto.ChatStreamEvent> tokenStream = llmService.stream(OllamaChatServiceImpl.defaultSystemPrompt(), userPrompt)
            .doOnNext(fullAnswer::append)
            .map(text -> com.nilesh.knowledgebase.dto.ChatStreamEvent.builder().type("CHUNK").text(text).build());
            
        reactor.core.publisher.Flux<com.nilesh.knowledgebase.dto.ChatStreamEvent> doneEvent = reactor.core.publisher.Flux.defer(() -> {
            long generationTimeMs = System.currentTimeMillis() - generationStartTime;
            long totalTimeMs = System.currentTimeMillis() - startTime;
            
            // Save assistant message in a try-catch so stream always completes cleanly
            try {
                com.nilesh.knowledgebase.entity.ChatMessage assistantMsg = new com.nilesh.knowledgebase.entity.ChatMessage();
                // Re-fetch the conversation to avoid detached entity issues
                com.nilesh.knowledgebase.entity.Conversation freshConversation = chatHistoryService.getConversation(conversationId, userId);
                assistantMsg.setConversation(freshConversation);
                assistantMsg.setRole("assistant");
                assistantMsg.setContent(fullAnswer.toString());
                assistantMsg.setConfidence(confidence);
                assistantMsg.setRetrievalCount(retrieved.size());
                assistantMsg.setTopScore(topScore);
                assistantMsg.setRetrievalTimeMs(retrievalTimeMs);
                assistantMsg.setGenerationTimeMs(generationTimeMs);
                assistantMsg.setTotalTimeMs(totalTimeMs);
                
                List<com.nilesh.knowledgebase.entity.ChatCitation> citations = contextChunks.stream().map(chunk -> {
                    com.nilesh.knowledgebase.entity.ChatCitation citation = new com.nilesh.knowledgebase.entity.ChatCitation();
                    com.nilesh.knowledgebase.entity.Document doc = documentRepository.getReferenceById(chunk.documentId());
                    citation.setDocument(doc);
                    citation.setChunkIndex(chunk.chunkIndex());
                    citation.setScore(chunk.score());
                    return citation;
                }).toList();
                
                chatHistoryService.saveMessageWithCitations(assistantMsg, citations);
                System.out.println("[SSE Backend] Successfully saved assistant message and citations");
            } catch (Exception e) {
                System.err.println("[SSE Backend] Failed to save assistant message: " + e.getMessage());
                e.printStackTrace();
                // Don't re-throw - let the stream complete normally
            }

            return reactor.core.publisher.Flux.just(com.nilesh.knowledgebase.dto.ChatStreamEvent.builder()
                .type("DONE")
                .generationTimeMs(generationTimeMs)
                .totalTimeMs(totalTimeMs)
                .build());
        });
        
        return reactor.core.publisher.Flux.concat(reactor.core.publisher.Flux.just(metadataEvent), tokenStream, doneEvent)
            .onErrorResume(e -> {
                System.err.println("[SSE Backend] Stream error: " + e.getMessage());
                e.printStackTrace();
                return reactor.core.publisher.Flux.just(com.nilesh.knowledgebase.dto.ChatStreamEvent.builder()
                    .type("ERROR")
                    .text("Stream error: " + e.getMessage())
                    .build());
            });
    }

    private ChatResponse insufficientContextResponse() {
        return new ChatResponse(RetrievalConfidence.REFUSAL_ANSWER, List.of(), "none", 0);
    }

    private List<ChatSource> buildSources(List<RetrievedChunk> chunks) {
        return java.util.stream.IntStream.range(0, chunks.size())
                .mapToObj(index -> toSource(index + 1, chunks.get(index)))
                .toList();
    }

    private ChatSource toSource(int sourceIndex, RetrievedChunk chunk) {
        return new ChatSource(
                sourceIndex,
                chunk.documentId(),
                chunk.documentTitle(),
                chunk.chunkIndex(),
                chunk.score(),
                buildExcerpt(chunk.content())
        );
    }

    private String buildExcerpt(String content) {
        int maxLength = ragProperties.getExcerptLength();
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength).trim() + "...";
    }
}

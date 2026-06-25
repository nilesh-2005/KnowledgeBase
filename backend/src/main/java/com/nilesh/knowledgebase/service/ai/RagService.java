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

    private final SimilaritySearchService similaritySearchService;
    private final PromptBuilderService promptBuilderService;
    private final LlmService llmService;
    private final RagProperties ragProperties;

    public RagService(SimilaritySearchService similaritySearchService,
                      PromptBuilderService promptBuilderService,
                      LlmService llmService,
                      RagProperties ragProperties) {
        this.similaritySearchService = similaritySearchService;
        this.promptBuilderService = promptBuilderService;
        this.llmService = llmService;
        this.ragProperties = ragProperties;
    }

    @Transactional(readOnly = true)
    public ChatResponse ask(UUID userId, String question, Integer topK) {
        int effectiveTopK = topK != null ? topK : ragProperties.getTopK();
        List<RetrievedChunk> retrieved = similaritySearchService.searchForUser(userId, question, effectiveTopK);

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

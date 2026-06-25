package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.config.RagProperties;
import com.nilesh.knowledgebase.dto.RetrievedChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PromptBuilderService {

    private static final String USER_PROMPT_TEMPLATE = """
            Context:
            %s

            Question:
            %s

            Answer using only the provided context.
            """;

    private final RagProperties ragProperties;

    public PromptBuilderService(RagProperties ragProperties) {
        this.ragProperties = ragProperties;
    }

    public String buildContext(List<RetrievedChunk> chunks) {
        List<RetrievedChunk> selected = selectWithinBudget(chunks);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < selected.size(); i++) {
            RetrievedChunk chunk = selected.get(i);
            builder.append("[SOURCE ").append(i + 1).append("]\n");
            builder.append("Document: ").append(chunk.documentTitle()).append('\n');
            builder.append("Chunk: ").append(chunk.chunkIndex()).append('\n');
            builder.append("Content:\n");
            builder.append(chunk.content()).append("\n---\n");
        }

        return builder.toString().trim();
    }

    public String buildUserPrompt(String question, String context) {
        return USER_PROMPT_TEMPLATE.formatted(context, question.trim());
    }

    public List<RetrievedChunk> selectChunksForContext(List<RetrievedChunk> chunks) {
        return selectWithinBudget(chunks);
    }

    private List<RetrievedChunk> selectWithinBudget(List<RetrievedChunk> chunks) {
        List<RetrievedChunk> selected = new ArrayList<>();
        int usedChars = 0;

        for (RetrievedChunk chunk : chunks) {
            int blockLength = estimateBlockLength(chunk);
            if (!selected.isEmpty() && usedChars + blockLength > ragProperties.getMaxContextChars()) {
                break;
            }
            selected.add(chunk);
            usedChars += blockLength;
        }

        return selected;
    }

    private int estimateBlockLength(RetrievedChunk chunk) {
        return chunk.content().length()
                + chunk.documentTitle().length()
                + 64;
    }
}

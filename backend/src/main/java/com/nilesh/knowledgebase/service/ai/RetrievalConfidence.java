package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.dto.RetrievedChunk;

import java.util.List;

public final class RetrievalConfidence {

    static final String REFUSAL_ANSWER =
            "I don't have enough information in the knowledge base to answer that.";

    private static final double HIGH_THRESHOLD = 0.75;
    private static final double MEDIUM_THRESHOLD = 0.60;
    private static final double LOW_THRESHOLD = 0.45;

    private RetrievalConfidence() {
    }

    public static String classify(double topScore) {
        if (topScore >= HIGH_THRESHOLD) {
            return "high";
        }
        if (topScore >= MEDIUM_THRESHOLD) {
            return "medium";
        }
        if (topScore >= LOW_THRESHOLD) {
            return "low";
        }
        return "none";
    }

    public static boolean hasUsableRetrieval(List<RetrievedChunk> chunks, double configuredScoreThreshold) {
        if (chunks.isEmpty()) {
            return false;
        }
        return chunks.stream().anyMatch(chunk -> chunk.score() >= configuredScoreThreshold)
                && chunks.getFirst().score() >= LOW_THRESHOLD;
    }

    public static boolean isRefusalAnswer(String answer) {
        if (answer == null) {
            return false;
        }
        return answer.trim().equals(REFUSAL_ANSWER);
    }
}

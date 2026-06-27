package com.nilesh.knowledgebase.dto;

public record SemanticSearchResult(
        java.util.UUID documentId,
        String documentTitle,
        int chunkIndex,
        double score,
        String content,
        String mode,
        double semanticScore,
        double keywordScore
) {}

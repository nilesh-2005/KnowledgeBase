package com.nilesh.knowledgebase.dto;

public record SemanticSearchResult(
        String documentTitle,
        int chunkIndex,
        double score,
        String content
) {}

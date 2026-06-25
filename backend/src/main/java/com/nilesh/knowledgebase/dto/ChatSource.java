package com.nilesh.knowledgebase.dto;

import java.util.UUID;

public record ChatSource(
        int sourceIndex,
        UUID documentId,
        String documentTitle,
        int chunkIndex,
        double score,
        String excerpt
) {}

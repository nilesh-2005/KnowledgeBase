package com.nilesh.knowledgebase.dto;

import java.util.UUID;

public record DocumentChunkResponse(
        UUID id,
        int chunkIndex,
        String content,
        Integer characterStart,
        Integer characterEnd,
        Integer tokenCount,
        boolean embeddingExists
) {}

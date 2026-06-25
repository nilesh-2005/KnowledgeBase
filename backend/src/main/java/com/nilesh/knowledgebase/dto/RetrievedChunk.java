package com.nilesh.knowledgebase.dto;

import java.util.UUID;

public record RetrievedChunk(
        UUID chunkId,
        UUID documentId,
        String documentTitle,
        int chunkIndex,
        String content,
        double score,
        double distance
) {}

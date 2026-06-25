package com.nilesh.knowledgebase.dto;

import java.util.List;

public record ChatResponse(
        String answer,
        List<ChatSource> sources,
        String confidence,
        int retrievalCount
) {}

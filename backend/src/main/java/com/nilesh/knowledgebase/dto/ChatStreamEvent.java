package com.nilesh.knowledgebase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatStreamEvent {
    private String type; // "METADATA", "CHUNK", "DONE", "ERROR"
    private String text;
    private List<ChatSource> sources;
    private String confidence;
    private Integer retrievalCount;
    private Long retrievalTimeMs;
    private Long generationTimeMs;
    private Long totalTimeMs;
}

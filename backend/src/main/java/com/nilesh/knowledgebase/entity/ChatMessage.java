package com.nilesh.knowledgebase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Conversation conversation;

    @Column(nullable = false, length = 20)
    private String role; // "user" or "assistant"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 20)
    private String confidence;

    @Column(name = "retrieval_count")
    private Integer retrievalCount;

    @Column(name = "top_score")
    private Double topScore;

    @Column(name = "retrieval_time_ms")
    private Long retrievalTimeMs;

    @Column(name = "generation_time_ms")
    private Long generationTimeMs;

    @Column(name = "total_time_ms")
    private Long totalTimeMs;

    private String model;

    @Column(name = "prompt_version", length = 50)
    private String promptVersion;

    private Double temperature;

    @Column(name = "top_k")
    private Integer topK;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("score DESC")
    private List<ChatCitation> citations = new ArrayList<>();
}

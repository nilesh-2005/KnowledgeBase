package com.nilesh.knowledgebase.controller;

import com.nilesh.knowledgebase.dto.ApiResponse;
import com.nilesh.knowledgebase.dto.ChatRequest;
import com.nilesh.knowledgebase.dto.ChatResponse;
import com.nilesh.knowledgebase.dto.ChatStreamEvent;
import com.nilesh.knowledgebase.entity.Conversation;
import com.nilesh.knowledgebase.entity.ChatMessage;
import com.nilesh.knowledgebase.security.UserPrincipal;
import com.nilesh.knowledgebase.service.ChatHistoryService;
import com.nilesh.knowledgebase.service.ai.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;
    private final ChatHistoryService chatHistoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ChatRequest request) {
        ChatResponse response = ragService.ask(user.getId(), request.question(), request.topK());
        String message = response.retrievalCount() == 0 ? "Insufficient context" : "Answer generated";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<Conversation>> createConversation(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody java.util.Map<String, String> request) {
        String firstMessage = request.getOrDefault("firstMessage", "New Conversation");
        Conversation conv = chatHistoryService.createConversation(user.getId(), firstMessage);
        return ResponseEntity.ok(ApiResponse.success("Conversation created", conv));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<Page<Conversation>>> listConversations(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<Conversation> conversations = chatHistoryService.getUserConversations(user.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Conversations retrieved", conversations));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getConversationHistory(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id) {
        // verify ownership first
        chatHistoryService.getConversation(id, user.getId());
        List<ChatMessage> messages = chatHistoryService.getMessages(id);
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved", messages));
    }

    @PatchMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<Conversation>> updateConversation(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id,
            @RequestBody java.util.Map<String, Object> request) {
        String title = (String) request.get("title");
        Boolean archived = (Boolean) request.get("archived");
        Conversation conv = chatHistoryService.updateConversation(id, user.getId(), title, archived);
        return ResponseEntity.ok(ApiResponse.success("Conversation updated", conv));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id) {
        chatHistoryService.deleteConversation(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted", null));
    }

    @PostMapping(value = "/conversations/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatStreamEvent> streamMessage(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id,
            @Valid @RequestBody ChatRequest request) {
        System.out.println("[SSE Backend] Entered ChatController.streamMessage for conv " + id);
        return ragService.streamAsk(user.getId(), id, request.question(), request.topK())
            .doOnSubscribe(sub -> System.out.println("[SSE Backend] Client subscribed to Flux"))
            .doOnNext(event -> System.out.println("[SSE Backend] Emitting event: " + event.getType()))
            .doOnError(e -> System.out.println("[SSE Backend] Error in stream: " + e.getMessage()))
            .doOnComplete(() -> System.out.println("[SSE Backend] Stream completed normally"))
            .doOnCancel(() -> System.out.println("[SSE Backend] Stream cancelled by client"));
    }
}

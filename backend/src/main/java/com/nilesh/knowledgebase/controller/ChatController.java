package com.nilesh.knowledgebase.controller;

import com.nilesh.knowledgebase.dto.ApiResponse;
import com.nilesh.knowledgebase.dto.ChatRequest;
import com.nilesh.knowledgebase.dto.ChatResponse;
import com.nilesh.knowledgebase.security.UserPrincipal;
import com.nilesh.knowledgebase.service.ai.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ChatRequest request) {
        ChatResponse response = ragService.ask(user.getId(), request.question(), request.topK());
        String message = response.retrievalCount() == 0 ? "Insufficient context" : "Answer generated";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}

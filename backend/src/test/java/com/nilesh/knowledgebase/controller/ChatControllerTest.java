package com.nilesh.knowledgebase.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nilesh.knowledgebase.dto.ChatResponse;
import com.nilesh.knowledgebase.dto.ChatSource;
import com.nilesh.knowledgebase.entity.Role;
import com.nilesh.knowledgebase.entity.User;
import com.nilesh.knowledgebase.security.UserPrincipal;
import com.nilesh.knowledgebase.service.ai.RagService;
import com.nilesh.knowledgebase.service.ChatHistoryService;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private RagService ragService;

    @Mock
    private ChatHistoryService chatHistoryService;

    private MockMvc mockMvc;
    private UUID userId;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();
        principal = UserPrincipal.from(user);

        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(ragService, chatHistoryService))
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter,
                                                  ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest,
                                                  WebDataBinderFactory binderFactory) {
                        return principal;
                    }
                })
                .build();
    }

    @Test
    void chatReturnsAnswerPayload() throws Exception {
        UUID documentId = UUID.randomUUID();
        when(ragService.ask(userId, "What is manipulation?", 5))
                .thenReturn(new ChatResponse(
                        "Manipulation is influence without consent. [SOURCE 1]",
                        List.of(new ChatSource(1, documentId, "Dark Psychology", 105, 0.82, "Excerpt")),
                        "high",
                        1
                ));

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "question": "What is manipulation?",
                      "topK": 5
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.answer").value("Manipulation is influence without consent. [SOURCE 1]"))
            .andExpect(jsonPath("$.data.sources[0].documentTitle").value("Dark Psychology"))
            .andExpect(jsonPath("$.data.confidence").value("high"));

        verify(ragService).ask(userId, "What is manipulation?", 5);
    }

    @Test
    void chatReturnsRefusalPayload() throws Exception {
        when(ragService.ask(userId, "unknown", 5))
                .thenReturn(new ChatResponse(
                        "I don't have enough information in the knowledge base to answer that.",
                        List.of(),
                        "none",
                        0
                ));

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "question": "unknown",
                      "topK": 5
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.confidence").value("none"))
                .andExpect(jsonPath("$.data.retrievalCount").value(0));
    }
}

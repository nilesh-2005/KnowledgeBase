package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.config.RagProperties;
import com.nilesh.knowledgebase.dto.RetrievedChunk;
import com.nilesh.knowledgebase.exception.LlmServiceUnavailableException;
import com.nilesh.knowledgebase.exception.LlmTimeoutException;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class OllamaChatServiceImpl implements LlmService {

    private static final String SYSTEM_PROMPT = """
            You are a knowledge base assistant.

            Answer ONLY using the provided context.

            Rules:
            - Do not use outside knowledge.
            - Do not invent facts.
            - Do not invent document names.
            - If the context is insufficient, respond exactly:
              "I don't have enough information in the knowledge base to answer that."
            - Cite sources using [SOURCE N].
            - Be concise and factual.
            """;

    private final ChatModel chatModel;
    private final RagProperties ragProperties;

    public OllamaChatServiceImpl(ChatModel chatModel, RagProperties ragProperties) {
        this.chatModel = chatModel;
        this.ragProperties = ragProperties;
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userPrompt)
        ));

        try {
            return CompletableFuture
                    .supplyAsync(() -> chatModel.call(prompt).getResult().getOutput().getText())
                    .get(ragProperties.getLlmTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (TimeoutException exception) {
            throw new LlmTimeoutException("Answer generation timed out", exception);
        } catch (ExecutionException exception) {
            throw mapExecutionException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LlmServiceUnavailableException("Answer generation was interrupted", exception);
        }
    }

    public static String defaultSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    private LlmServiceUnavailableException mapExecutionException(ExecutionException exception) {
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        if (cause instanceof ResourceAccessException || cause instanceof ConnectException) {
            return new LlmServiceUnavailableException("AI service temporarily unavailable", cause);
        }
        if (isUnavailableMessage(cause.getMessage())) {
            return new LlmServiceUnavailableException("AI service temporarily unavailable", cause);
        }
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new LlmServiceUnavailableException("AI service temporarily unavailable", cause);
    }

    private boolean isUnavailableMessage(String message) {
        if (message == null) {
            return false;
        }
        String lower = message.toLowerCase();
        return lower.contains("connection refused")
                || lower.contains("connect")
                || lower.contains("404")
                || lower.contains("model")
                || lower.contains("unavailable");
    }
}

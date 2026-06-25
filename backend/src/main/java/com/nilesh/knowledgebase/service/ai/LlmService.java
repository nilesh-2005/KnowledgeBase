package com.nilesh.knowledgebase.service.ai;

public interface LlmService {

    String generate(String systemPrompt, String userPrompt);
}

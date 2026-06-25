import React, { useCallback, useEffect, useRef, useState } from 'react';
import { apiClient, type ChatSource } from '../../lib/api';
import { ChatApiError } from '../../lib/chatErrors';
import { ChatEmptyState } from './ChatEmptyState';
import { ChatInput } from './ChatInput';
import { ChatMessage, type ChatMessageData } from './ChatMessage';
import { SourcePreviewModal } from './SourcePreviewModal';

function createId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
}

export function AiAssistantWrapper() {
  const [messages, setMessages] = useState<ChatMessageData[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [selectedSource, setSelectedSource] = useState<ChatSource | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const scrollContainerRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  const submitQuestion = useCallback(async (question: string) => {
    const trimmed = question.trim();
    if (!trimmed || loading) {
      return;
    }

    const userMessage: ChatMessageData = {
      id: createId(),
      role: 'user',
      content: trimmed,
    };

    const thinkingId = createId();
    const thinkingMessage: ChatMessageData = {
      id: thinkingId,
      role: 'assistant',
      content: '',
      isThinking: true,
    };

    setMessages((current) => [...current, userMessage, thinkingMessage]);
    setInput('');
    setLoading(true);

    try {
      const response = await apiClient.sendChatMessage({ question: trimmed, topK: 5 });

      if (!response.success || !response.data) {
        throw new Error(response.message || 'Failed to generate an answer.');
      }

      const assistantMessage: ChatMessageData = {
        id: thinkingId,
        role: 'assistant',
        content: response.data.answer,
        sources: response.data.sources,
        confidence: response.data.confidence,
        retrievalCount: response.data.retrievalCount,
      };

      setMessages((current) =>
        current.map((message) => (message.id === thinkingId ? assistantMessage : message))
      );
    } catch (error) {
      const errorMessage =
        error instanceof ChatApiError
          ? error.message
          : error instanceof Error
            ? error.message
            : 'Something went wrong. Please try again.';

      const errorResponse: ChatMessageData = {
        id: thinkingId,
        role: 'assistant',
        content: errorMessage,
        isError: true,
      };

      setMessages((current) =>
        current.map((message) => (message.id === thinkingId ? errorResponse : message))
      );
    } finally {
      setLoading(false);
    }
  }, [loading]);

  const handleExampleClick = (question: string) => {
    setInput(question);
    void submitQuestion(question);
  };

  return (
    <div className="-mx-4 -mt-5 flex h-[calc(100vh-3.5rem)] flex-col sm:-mx-6 lg:-mx-8">
      <div
        ref={scrollContainerRef}
        className="flex-1 overflow-y-auto bg-canvas"
        aria-live="polite"
        aria-relevant="additions"
      >
        {messages.length === 0 ? (
          <ChatEmptyState onExampleClick={handleExampleClick} disabled={loading} />
        ) : (
          <div>
            {messages.map((message) => (
              <ChatMessage
                key={message.id}
                message={message}
                onSourceSelect={setSelectedSource}
              />
            ))}
            <div ref={messagesEndRef} />
          </div>
        )}
      </div>

      <div className="shrink-0 border-t border-border bg-panel px-4 py-3 sm:px-6">
        <ChatInput
          value={input}
          onChange={setInput}
          onSubmit={() => void submitQuestion(input)}
          disabled={loading}
        />
      </div>

      <SourcePreviewModal source={selectedSource} onClose={() => setSelectedSource(null)} />
    </div>
  );
}

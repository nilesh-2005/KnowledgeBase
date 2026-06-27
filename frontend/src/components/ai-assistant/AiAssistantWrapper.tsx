import React, { useCallback, useEffect, useRef, useState } from 'react';
import { apiClient, streamChatMessage, type ChatSource, type Conversation } from '../../lib/api';
import { ChatEmptyState } from './ChatEmptyState';
import { ChatInput } from './ChatInput';
import { ChatMessage, type ChatMessageData } from './ChatMessage';
import { SourcePreviewModal } from './SourcePreviewModal';
import { ConversationSidebar } from './ConversationSidebar';

function createId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
}

export function AiAssistantWrapper() {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessageData[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const scrollContainerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadConversations();
  }, []);

  const loadConversations = async () => {
    try {
      const res = await apiClient.getConversations();
      if (res.success && res.data) {
        setConversations(res.data.content);
      }
    } catch (e) {
      console.error('Failed to load conversations', e);
    }
  };

  const loadMessages = async (id: string) => {
    setLoading(true);
    try {
      const res = await apiClient.getConversationMessages(id);
      if (res.success && res.data) {
        const loaded: ChatMessageData[] = res.data.map(m => ({
          id: m.id,
          role: m.role,
          content: m.content,
          confidence: m.confidence,
          retrievalCount: m.retrievalCount,
          sources: m.citations?.map((c, i) => ({
            sourceIndex: i + 1,
            documentId: c.document.id,
            documentTitle: c.document.title,
            chunkIndex: c.chunkIndex,
            score: c.score,
            excerpt: '',
          }))
        }));
        setMessages(loaded);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectConversation = (id: string) => {
    setActiveConversationId(id);
    loadMessages(id);
  };

  const handleNewConversation = () => {
    setActiveConversationId(null);
    setMessages([]);
    setInput('');
  };

  const scrollToBottom = useCallback(() => {
    setTimeout(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }, 50);
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  const submitQuestion = useCallback(async (question: string) => {
    const trimmed = question.trim();
    if (!trimmed || loading) return;

    let convId = activeConversationId;
    if (!convId) {
      try {
        const res = await apiClient.createConversation(trimmed);
        if (res.success && res.data) {
          convId = res.data.id;
          setActiveConversationId(convId);
          loadConversations();
        } else {
          throw new Error('Failed to create conversation');
        }
      } catch (e) {
        console.error(e);
        return;
      }
    }

    const userMessage: ChatMessageData = {
      id: createId(),
      role: 'user',
      content: trimmed,
    };

    const thinkingId = createId();
    let assistantMessage: ChatMessageData = {
      id: thinkingId,
      role: 'assistant',
      content: '',
      isThinking: true,
    };

    setMessages((current) => [...current, userMessage, assistantMessage]);
    setInput('');
    setLoading(true);

    try {
      await streamChatMessage(convId!, { question: trimmed, topK: 5 }, (event) => {
        if (event.type === 'METADATA') {
          assistantMessage = {
            ...assistantMessage,
            isThinking: false,
            confidence: event.confidence,
            retrievalCount: event.retrievalCount,
            sources: event.sources,
          };
          setMessages(current => current.map(m => m.id === thinkingId ? assistantMessage : m));
        } else if (event.type === 'CHUNK') {
          assistantMessage = {
            ...assistantMessage,
            isThinking: false,
            content: assistantMessage.content + (event.text || ''),
          };
          setMessages(current => current.map(m => m.id === thinkingId ? assistantMessage : m));
        } else if (event.type === 'ERROR') {
          assistantMessage = {
            ...assistantMessage,
            isThinking: false,
            isError: true,
            content: event.text || 'An error occurred.',
          };
          setMessages(current => current.map(m => m.id === thinkingId ? assistantMessage : m));
        }
      });
    } catch (error) {
       console.error('[SSE] Stream error:', error);
       // Only show error if we never received any content
       if (!assistantMessage.content) {
         assistantMessage = {
           ...assistantMessage,
           isThinking: false,
           isError: true,
           content: 'Failed to connect to streaming API.',
         };
       } else {
         // We already have streamed content — keep it, just stop thinking
         assistantMessage = {
           ...assistantMessage,
           isThinking: false,
         };
       }
       setMessages(current => current.map(m => m.id === thinkingId ? assistantMessage : m));
    } finally {
      setLoading(false);
    }
  }, [activeConversationId, loading]);

  const handleExampleClick = (question: string) => {
    setInput(question);
    void submitQuestion(question);
  };

  const currentConv = conversations.find(c => c.id === activeConversationId);

  return (
    <div className="-mx-4 -mt-5 flex h-[calc(100vh-3.5rem)] sm:-mx-6 lg:-mx-8">
      <ConversationSidebar
        conversations={conversations}
        activeId={activeConversationId}
        onSelect={handleSelectConversation}
        onNew={handleNewConversation}
      />
      <div className="flex flex-1 flex-col overflow-hidden bg-canvas relative">
        <div
          ref={scrollContainerRef}
          className="flex-1 overflow-y-auto"
          aria-live="polite"
          aria-relevant="additions"
        >
          {messages.length === 0 ? (
            <ChatEmptyState onExampleClick={handleExampleClick} disabled={loading} />
          ) : (
            <div className="py-2">
              <div className="mx-auto max-w-3xl px-4 sm:px-6 mb-2">
                <h2 className="text-[11px] font-semibold uppercase tracking-wider text-text-subtle">
                  Conversation
                </h2>
                <p className="text-sm font-medium text-text-main mt-0.5 truncate">
                  {currentConv?.title || 'New Conversation'}
                </p>
              </div>
              {messages.map((message) => (
                <ChatMessage
                  key={message.id}
                  message={message}
                />
              ))}
              <div ref={messagesEndRef} className="h-4" />
            </div>
          )}
        </div>

        <div className="shrink-0 border-t border-border bg-panel px-4 py-2 sm:px-6">
          <ChatInput
            value={input}
            onChange={setInput}
            onSubmit={() => void submitQuestion(input)}
            disabled={loading}
          />
        </div>
      </div>
    </div>
  );
}

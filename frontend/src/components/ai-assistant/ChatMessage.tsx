import React from 'react';
import { AlertCircle } from 'lucide-react';
import type { ChatConfidence, ChatSource } from '../../lib/api';
import { ConfidenceBadge } from './ConfidenceBadge';
import { MarkdownContent } from './MarkdownContent';
import { SourceList } from './SourceList';

export interface ChatMessageData {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  sources?: ChatSource[];
  confidence?: ChatConfidence;
  retrievalCount?: number;
  isError?: boolean;
  isThinking?: boolean;
}

interface ChatMessageProps {
  message: ChatMessageData;
}

export function ChatMessage({ message }: ChatMessageProps) {
  if (message.role === 'user') {
    return (
      <article className="border-b border-border/50 px-4 py-2.5 sm:px-6">
        <div className="mx-auto max-w-3xl">
          <div className="mb-0.5 text-[11px] font-semibold uppercase tracking-wider text-text-subtle">You</div>
          <div className="whitespace-pre-wrap text-sm text-text-main">{message.content}</div>
        </div>
      </article>
    );
  }

  return (
    <article className="border-b border-border/50 bg-panel/40 px-4 py-3 sm:px-6">
      <div className="mx-auto max-w-3xl">
        <div className="mb-2">
          <div className="mb-1 text-[11px] font-semibold uppercase tracking-wider text-text-subtle">Assistant</div>
          {(!message.isThinking && !message.isError && (message.confidence || message.retrievalCount != null)) && (
            <div className="flex flex-wrap items-center gap-2">
              {message.confidence && (
                <ConfidenceBadge confidence={message.confidence} />
              )}
              {message.confidence && message.retrievalCount != null && message.retrievalCount > 0 && (
                <span className="text-text-muted text-[10px] opacity-60">•</span>
              )}
              {message.retrievalCount != null && message.retrievalCount > 0 && (
                <span className="text-[11px] font-medium text-text-muted">
                  {message.retrievalCount} source{message.retrievalCount === 1 ? '' : 's'}
                </span>
              )}
            </div>
          )}
        </div>

        {message.isThinking ? (
          <div className="flex items-center gap-2 text-sm text-text-muted" aria-live="polite">
            <span>Thinking</span>
            <span className="inline-flex gap-1" aria-hidden="true">
              <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-text-subtle [animation-delay:0ms]" />
              <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-text-subtle [animation-delay:150ms]" />
              <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-text-subtle [animation-delay:300ms]" />
            </span>
          </div>
        ) : message.isError ? (
          <div className="flex items-start gap-2 rounded border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
            <span>{message.content}</span>
          </div>
        ) : (
          <>
            <MarkdownContent 
              content={message.content} 
              sources={message.sources}
            />
            {message.sources && message.sources.length > 0 && (
              <SourceList sources={message.sources} />
            )}
          </>
        )}
      </div>
    </article>
  );
}

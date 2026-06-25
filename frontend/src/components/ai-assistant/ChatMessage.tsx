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
  onSourceSelect: (source: ChatSource) => void;
}

export function ChatMessage({ message, onSourceSelect }: ChatMessageProps) {
  if (message.role === 'user') {
    return (
      <article className="border-b border-border px-4 py-3 sm:px-6">
        <div className="mx-auto max-w-3xl">
          <div className="mb-1 text-xs font-medium uppercase tracking-wide text-text-subtle">You</div>
          <div className="whitespace-pre-wrap text-sm text-text-main">{message.content}</div>
        </div>
      </article>
    );
  }

  return (
    <article className="border-b border-border bg-panel px-4 py-3 sm:px-6">
      <div className="mx-auto max-w-3xl">
        <div className="mb-2 flex flex-wrap items-center gap-2">
          <span className="text-xs font-medium uppercase tracking-wide text-text-subtle">Assistant</span>
          {message.confidence && !message.isThinking && !message.isError && (
            <ConfidenceBadge confidence={message.confidence} />
          )}
          {message.retrievalCount != null && message.retrievalCount > 0 && !message.isThinking && (
            <span className="text-xs text-text-muted">
              {message.retrievalCount} chunk{message.retrievalCount === 1 ? '' : 's'} retrieved
            </span>
          )}
        </div>

        {message.isThinking ? (
          <div className="flex items-center gap-2 text-sm text-text-muted" aria-live="polite">
            <span>Thinking</span>
            <span className="inline-flex gap-0.5" aria-hidden="true">
              <span className="h-1 w-1 animate-pulse rounded-full bg-text-subtle [animation-delay:0ms]" />
              <span className="h-1 w-1 animate-pulse rounded-full bg-text-subtle [animation-delay:150ms]" />
              <span className="h-1 w-1 animate-pulse rounded-full bg-text-subtle [animation-delay:300ms]" />
            </span>
          </div>
        ) : message.isError ? (
          <div className="flex items-start gap-2 rounded border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
            <span>{message.content}</span>
          </div>
        ) : (
          <>
            <MarkdownContent content={message.content} />
            {message.sources && message.sources.length > 0 && (
              <SourceList sources={message.sources} onSelect={onSourceSelect} />
            )}
          </>
        )}
      </div>
    </article>
  );
}

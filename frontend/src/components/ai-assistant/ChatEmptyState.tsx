import React from 'react';
import { MessageSquareText } from 'lucide-react';

const EXAMPLE_QUESTIONS = [
  'What is psychological manipulation?',
  'What are common influence tactics described in my documents?',
  'Summarize the key topics in my uploaded knowledge base.',
] as const;

interface ChatEmptyStateProps {
  onExampleClick: (question: string) => void;
  disabled?: boolean;
}

export function ChatEmptyState({ onExampleClick, disabled = false }: ChatEmptyStateProps) {
  return (
    <div className="flex flex-1 flex-col items-center justify-center px-4 py-8 sm:px-6">
      <div className="mx-auto w-full max-w-xl text-center">
        <div className="mx-auto flex h-10 w-10 items-center justify-center rounded-md border border-border bg-surface">
          <MessageSquareText className="h-5 w-5 text-text-muted" aria-hidden="true" />
        </div>
        <h2 className="mt-3 text-base font-semibold text-text-main">AI Assistant</h2>
        <p className="mt-1 text-sm text-text-muted">
          Ask questions about your uploaded knowledge base documents.
        </p>
        <div className="mt-6 text-left">
          <div className="mb-2 text-xs font-medium uppercase tracking-wide text-text-subtle">
            Example questions
          </div>
          <ul className="space-y-1.5">
            {EXAMPLE_QUESTIONS.map((question) => (
              <li key={question}>
                <button
                  type="button"
                  disabled={disabled}
                  onClick={() => onExampleClick(question)}
                  className="w-full rounded border border-border bg-panel px-3 py-2 text-left text-sm text-text-main transition-colors hover:bg-surface disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {question}
                </button>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}

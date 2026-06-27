import React from 'react';
import { Send } from 'lucide-react';
import { Button } from '../ui/Button';

interface ChatInputProps {
  value: string;
  onChange: (value: string) => void;
  onSubmit: () => void;
  disabled?: boolean;
  placeholder?: string;
}

export function ChatInput({
  value,
  onChange,
  onSubmit,
  disabled = false,
  placeholder = 'Ask a question about your knowledge base...',
}: ChatInputProps) {
  const textareaRef = React.useRef<HTMLTextAreaElement>(null);

  React.useEffect(() => {
    const textarea = textareaRef.current;
    if (!textarea) {
      return;
    }
    textarea.style.height = 'auto';
    textarea.style.height = `${Math.min(textarea.scrollHeight, 160)}px`;
  }, [value]);

  const handleKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter') {
      if (event.ctrlKey || event.metaKey || !event.shiftKey) {
        event.preventDefault();
        if (!disabled && value.trim()) {
          onSubmit();
        }
      }
    }
  };

  return (
    <form
      className="mx-auto flex max-w-3xl items-end gap-2"
      onSubmit={(event) => {
        event.preventDefault();
        if (!disabled && value.trim()) {
          onSubmit();
        }
      }}
    >
      <div className="min-w-0 flex-1">
        <label htmlFor="ai-assistant-input" className="sr-only">
          Ask a question
        </label>
        <textarea
          ref={textareaRef}
          id="ai-assistant-input"
          rows={1}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          onKeyDown={handleKeyDown}
          disabled={disabled}
          placeholder={placeholder}
          className="block max-h-40 min-h-[40px] w-full resize-none rounded-md border border-border bg-panel px-3 py-2.5 text-[13px] text-text-main placeholder:text-text-subtle focus:border-border-focus focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-border-focus/20 disabled:cursor-not-allowed disabled:bg-surface disabled:opacity-70"
        />
      </div>
      <Button
        type="submit"
        size="md"
        disabled={disabled || !value.trim()}
        aria-label="Send question"
        className="shrink-0"
      >
        <Send className="h-4 w-4" />
        Send
      </Button>
    </form>
  );
}

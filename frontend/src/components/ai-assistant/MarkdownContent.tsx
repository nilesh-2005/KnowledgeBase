import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { cn } from '../../lib/cn';

interface MarkdownContentProps {
  content: string;
  className?: string;
}

export function MarkdownContent({ content, className }: MarkdownContentProps) {
  return (
    <div
      className={cn(
        'prose-kb text-sm leading-relaxed text-text-main',
        className
      )}
    >
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          p: ({ children }) => <p className="mb-2 last:mb-0">{children}</p>,
          ul: ({ children }) => <ul className="mb-2 list-disc pl-5 last:mb-0">{children}</ul>,
          ol: ({ children }) => <ol className="mb-2 list-decimal pl-5 last:mb-0">{children}</ol>,
          li: ({ children }) => <li className="mb-0.5">{children}</li>,
          h1: ({ children }) => <h3 className="mb-2 mt-3 text-base font-semibold first:mt-0">{children}</h3>,
          h2: ({ children }) => <h3 className="mb-2 mt-3 text-sm font-semibold first:mt-0">{children}</h3>,
          h3: ({ children }) => <h4 className="mb-1.5 mt-2 text-sm font-semibold first:mt-0">{children}</h4>,
          code: ({ className: codeClassName, children }) => {
            const isBlock = codeClassName?.includes('language-');
            if (isBlock) {
              return (
                <pre className="my-2 overflow-x-auto rounded border border-border bg-surface px-3 py-2 text-xs">
                  <code>{children}</code>
                </pre>
              );
            }
            return (
              <code className="rounded border border-border bg-surface px-1 py-0.5 font-mono text-xs">
                {children}
              </code>
            );
          },
          pre: ({ children }) => <>{children}</>,
          a: ({ href, children }) => (
            <a href={href} className="text-primary underline underline-offset-2 hover:text-blue-700" target="_blank" rel="noopener noreferrer">
              {children}
            </a>
          ),
          blockquote: ({ children }) => (
            <blockquote className="my-2 border-l-2 border-border pl-3 text-text-muted">{children}</blockquote>
          ),
          strong: ({ children }) => <strong className="font-semibold">{children}</strong>,
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}

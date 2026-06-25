import React from 'react';
import { FileText } from 'lucide-react';
import type { ChatSource } from '../../lib/api';

interface SourceListProps {
  sources: ChatSource[];
  onSelect: (source: ChatSource) => void;
}

export function SourceList({ sources, onSelect }: SourceListProps) {
  if (sources.length === 0) {
    return null;
  }

  return (
    <div className="mt-3 border-t border-border pt-3">
      <div className="mb-2 text-xs font-medium uppercase tracking-wide text-text-subtle">
        Sources ({sources.length})
      </div>
      <ul className="space-y-1.5">
        {sources.map((source) => (
          <li key={`${source.documentId}-${source.chunkIndex}-${source.sourceIndex}`}>
            <button
              type="button"
              onClick={() => onSelect(source)}
              className="flex w-full items-start gap-2.5 rounded border border-border bg-surface px-3 py-2 text-left transition-colors hover:bg-surface-hover focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-border-focus"
            >
              <FileText className="mt-0.5 h-3.5 w-3.5 shrink-0 text-text-subtle" aria-hidden="true" />
              <span className="min-w-0 flex-1">
                <span className="block truncate text-sm font-medium text-text-main">
                  {source.documentTitle}
                </span>
                <span className="mt-0.5 block text-xs text-text-muted">
                  Chunk {source.chunkIndex} · Score {source.score.toFixed(2)}
                </span>
              </span>
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}

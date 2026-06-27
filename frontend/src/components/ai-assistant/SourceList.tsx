import React from 'react';
import { FileText } from 'lucide-react';
import type { ChatSource } from '../../lib/api';

interface SourceListProps {
  sources: ChatSource[];
}

export function SourceList({ sources }: SourceListProps) {
  if (sources.length === 0) {
    return null;
  }

  return (
    <div className="mt-3 pt-2">
      <div className="mb-1 text-[11px] font-semibold uppercase tracking-wider text-text-subtle">
        Sources
      </div>
      <ul className="space-y-0.5">
        {sources.map((source, index) => (
          <li key={`${source.documentId}-${source.chunkIndex}-${source.sourceIndex}`}>
            <a
              href={`/documents/${source.documentId}?chunk=${source.chunkIndex}`}
              className="flex w-full items-start gap-2 rounded px-1.5 py-1.5 text-left text-[13px] text-text-muted transition-colors hover:bg-border/30 hover:text-text-main focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-border-focus"
            >
              <span className="font-medium shrink-0 pt-0.5">[{index + 1}]</span>
              <div className="min-w-0 flex-1">
                <div className="truncate font-medium text-text-main">{source.documentTitle}</div>
                <div className="mt-0.5 text-[11px] text-text-muted">
                  Chunk {source.chunkIndex} <span className="mx-1 opacity-60">•</span> {source.score.toFixed(2)}
                </div>
              </div>
            </a>
          </li>
        ))}
      </ul>
    </div>
  );
}

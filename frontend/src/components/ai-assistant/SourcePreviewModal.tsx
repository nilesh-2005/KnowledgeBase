import React from 'react';
import { Modal } from '../ui/Modal';
import type { ChatSource } from '../../lib/api';

interface SourcePreviewModalProps {
  source: ChatSource | null;
  onClose: () => void;
}

export function SourcePreviewModal({ source, onClose }: SourcePreviewModalProps) {
  if (!source) {
    return null;
  }

  return (
    <Modal
      open={Boolean(source)}
      title={source.documentTitle}
      description={`Chunk ${source.chunkIndex} · Score ${source.score.toFixed(2)}`}
      onClose={onClose}
    >
      <div className="space-y-3">
        <div>
          <div className="text-xs font-medium uppercase tracking-wide text-text-subtle">Original excerpt</div>
          <blockquote className="mt-2 rounded border border-border bg-surface px-3 py-2 text-sm leading-relaxed text-text-main">
            &ldquo;{source.excerpt}&rdquo;
          </blockquote>
        </div>
        <div className="flex items-center justify-between border-t border-border pt-3 text-xs text-text-muted">
          <span>Source {source.sourceIndex}</span>
          <a
            href={`/documents/${source.documentId}`}
            className="font-medium text-primary hover:underline"
          >
            View document
          </a>
        </div>
      </div>
    </Modal>
  );
}

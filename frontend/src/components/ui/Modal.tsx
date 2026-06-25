import React from 'react';
import { X } from 'lucide-react';
import { cn } from '../../lib/cn';
import { Button } from './Button';

interface ModalProps {
  open: boolean;
  title: string;
  description?: string;
  children: React.ReactNode;
  onClose: () => void;
}

export function Modal({ open, title, description, children, onClose }: ModalProps) {
  React.useEffect(() => {
    if (!open) {
      return;
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        onClose();
      }
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [open, onClose]);

  if (!open) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4" role="presentation">
      <div className="absolute inset-0" onClick={onClose} />
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        className={cn('relative w-full max-w-lg rounded-md border border-border bg-panel shadow-lg')}
      >
        <header className="flex items-start justify-between gap-4 border-b border-border px-4 py-3">
          <div>
            <h2 id="modal-title" className="text-sm font-semibold text-text-main">
              {title}
            </h2>
            {description && <p className="mt-1 text-sm text-text-muted">{description}</p>}
          </div>
          <Button type="button" variant="ghost" size="sm" className="h-8 w-8 p-0" onClick={onClose}>
            <X className="h-4 w-4" />
            <span className="sr-only">Close modal</span>
          </Button>
        </header>
        <div className="p-4">{children}</div>
      </section>
    </div>
  );
}

import React from 'react';
import { cn } from '../../lib/cn';

export interface TextAreaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {}

export const TextArea = React.forwardRef<HTMLTextAreaElement, TextAreaProps>(
  ({ className = '', ...props }, ref) => (
    <textarea
      ref={ref}
      className={cn(
        'min-h-24 w-full rounded-md border border-border bg-panel px-3 py-2 text-sm text-text-main transition-colors placeholder:text-text-subtle focus:border-border-focus focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-border-focus/20 disabled:cursor-not-allowed disabled:bg-surface disabled:opacity-70',
        className
      )}
      {...props}
    />
  )
);

TextArea.displayName = 'TextArea';

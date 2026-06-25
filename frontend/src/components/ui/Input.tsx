import React from 'react';
import { cn } from '../../lib/cn';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className = '', type, ...props }, ref) => {
    return (
      <input
        type={type}
        className={cn(
          'flex h-9 w-full rounded-md border border-border bg-panel px-3 py-1 text-sm text-text-main transition-colors placeholder:text-text-subtle focus:border-border-focus focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-border-focus/20 disabled:cursor-not-allowed disabled:bg-surface disabled:opacity-70',
          className
        )}
        ref={ref}
        {...props}
      />
    );
  }
);
Input.displayName = 'Input';

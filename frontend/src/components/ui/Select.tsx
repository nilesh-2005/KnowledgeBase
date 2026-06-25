import React from 'react';
import { ChevronDown } from 'lucide-react';
import { cn } from '../../lib/cn';

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {}

export const Select = React.forwardRef<HTMLSelectElement, SelectProps>(
  ({ className = '', children, ...props }, ref) => (
    <div className="relative">
      <select
        ref={ref}
        className={cn(
          'h-9 w-full appearance-none rounded-md border border-border bg-panel px-3 pr-9 text-sm text-text-main transition-colors focus:border-border-focus focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-border-focus/20 disabled:cursor-not-allowed disabled:bg-surface disabled:opacity-70',
          className
        )}
        {...props}
      >
        {children}
      </select>
      <ChevronDown className="pointer-events-none absolute right-2.5 top-2.5 h-4 w-4 text-text-subtle" />
    </div>
  )
);

Select.displayName = 'Select';

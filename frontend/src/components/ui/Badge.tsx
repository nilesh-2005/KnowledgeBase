import React from 'react';
import { cn } from '../../lib/cn';

export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'secondary' | 'outline' | 'success' | 'warning' | 'error';
}

export function Badge({ className = '', variant = 'default', ...props }: BadgeProps) {
  const baseStyles = 'inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium leading-5';
  
  const variants = {
    default: 'border-blue-200 bg-blue-50 text-blue-700',
    secondary: 'border-border bg-surface text-text-muted',
    outline: 'border-border bg-panel text-text-main',
    success: 'border-green-200 bg-green-50 text-green-700',
    warning: 'border-amber-200 bg-amber-50 text-amber-700',
    error: 'border-red-200 bg-red-50 text-red-700',
  };

  return (
    <div className={cn(baseStyles, variants[variant], className)} {...props} />
  );
}

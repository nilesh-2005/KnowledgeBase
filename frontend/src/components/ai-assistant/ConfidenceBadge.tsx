import React from 'react';
import { Badge } from '../ui/Badge';
import type { ChatConfidence } from '../../lib/api';

interface ConfidenceBadgeProps {
  confidence: ChatConfidence;
}

const variantMap: Record<ChatConfidence, 'success' | 'warning' | 'error' | 'secondary'> = {
  high: 'success',
  medium: 'warning',
  low: 'error',
  none: 'secondary',
};

const labelMap: Record<ChatConfidence, string> = {
  high: 'High confidence',
  medium: 'Medium confidence',
  low: 'Low confidence',
  none: 'No confidence',
};

export function ConfidenceBadge({ confidence }: ConfidenceBadgeProps) {
  return (
    <Badge 
      variant={variantMap[confidence]} 
      aria-label={labelMap[confidence]}
      className="rounded-full px-2.5 py-0.5 text-[10px] font-semibold uppercase tracking-wider shadow-sm"
    >
      {labelMap[confidence]}
    </Badge>
  );
}

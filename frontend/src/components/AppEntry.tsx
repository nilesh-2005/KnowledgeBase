import React from 'react';
import { ArrowRight } from 'lucide-react';
import { useAuth } from '../lib/auth';

export function AppEntry() {
  const { isAuthenticated, isLoading } = useAuth();

  React.useEffect(() => {
    if (isLoading) {
      return;
    }

    window.location.href = isAuthenticated ? '/dashboard' : '/login';
  }, [isAuthenticated, isLoading]);

  return (
    <div className="mx-auto w-full max-w-md rounded-md border border-border bg-panel p-4">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-sm font-semibold text-text-main">Knowledge Base</h1>
          <p className="mt-1 text-sm text-text-muted">
            {isLoading ? 'Checking session...' : 'Opening workspace...'}
          </p>
        </div>
        <ArrowRight className="h-4 w-4 text-text-subtle" />
      </div>
    </div>
  );
}

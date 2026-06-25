import React, { useEffect } from 'react';
import { useAuth } from '../lib/auth';

export function AuthGuard({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      // Redirect to login if not authenticated
      window.location.href = '/login';
    }
  }, [isAuthenticated, isLoading]);

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-canvas">
        <div className="text-center">
          <p className="mb-4 text-sm font-medium text-text-main">Loading workspace</p>
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-2 border-border border-t-primary" />
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return <>{children}</>;
}

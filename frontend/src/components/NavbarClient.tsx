import React from 'react';
import { useAuth } from '../lib/auth';
import { UserMenu } from './UserMenu';

export function NavbarClient({ currentPath }: { currentPath?: string }) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return null;
  }

  return (
    <nav className="bg-canvas border-b border-hairline-soft">
      <div className="container-xl flex items-center justify-between h-14">
        <div className="flex items-center gap-6">
          <a href="/" className="text-heading-md font-bold text-primary hover:no-underline">
            Knowledge Base
          </a>
          <div className="hidden md:flex gap-4">
            {isAuthenticated && (
              <a 
                href="/dashboard" 
                className={`text-body-strong transition-colors hover:no-underline ${
                  currentPath === '/dashboard' 
                    ? 'text-primary border-b-2 border-primary' 
                    : 'text-mute hover:text-ink'
                }`}
              >
                Dashboard
              </a>
            )}
          </div>
        </div>
        
        <div className="flex items-center gap-4">
          {!isAuthenticated ? (
            <>
              <a href="/login" className="text-body-strong text-primary hover:text-charcoal hover:no-underline transition-colors">
                Sign In
              </a>
              <a href="/register" className="btn-primary text-sm">
                Sign Up
              </a>
            </>
          ) : (
            <UserMenu />
          )}
        </div>
      </div>
    </nav>
  );
}

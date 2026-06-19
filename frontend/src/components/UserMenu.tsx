import React from 'react';
import { useAuth } from '../lib/auth';

export function UserMenu() {
  const { user, logout, isAuthenticated } = useAuth();
  const [isOpen, setIsOpen] = React.useState(false);

  if (!isAuthenticated || !user) {
    return null;
  }

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 px-3 py-2 text-body-strong text-ink hover:bg-soft-cloud rounded-md transition-colors"
      >
        <span>{user.name}</span>
        <span className="text-sm">▼</span>
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-48 bg-canvas border border-hairline-soft rounded-md shadow-lg z-50">
          <div className="px-4 py-3 border-b border-hairline-soft">
            <p className="text-caption-md text-mute">{user.email}</p>
            {user.roles && user.roles.length > 0 && (
              <p className="text-caption-sm text-mute mt-1">
                {user.roles.join(', ')}
              </p>
            )}
          </div>
          <button
            onClick={() => {
              logout();
              window.location.href = '/login';
            }}
            className="block w-full text-left px-4 py-2 text-body-md text-sale hover:bg-soft-cloud transition-colors"
          >
            Sign Out
          </button>
        </div>
      )}
    </div>
  );
}

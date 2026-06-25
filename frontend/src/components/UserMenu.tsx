import React from 'react';
import { ChevronDown, LogOut, UserCircle } from 'lucide-react';
import { useAuth } from '../lib/auth';
import { Dropdown, DropdownContent, DropdownItem, DropdownTrigger } from './ui/Dropdown';
import { Badge } from './ui/Badge';

function initialsFromName(name: string) {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('');
}

export function UserMenu() {
  const { user, logout, isAuthenticated } = useAuth();

  if (!isAuthenticated || !user) {
    return null;
  }

  const initials = initialsFromName(user.fullName) || 'U';

  return (
    <Dropdown>
      <DropdownTrigger className="flex h-9 items-center gap-2 rounded-md border border-border bg-panel px-2 text-sm font-medium text-text-main hover:bg-surface focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-border-focus">
        <span className="flex h-6 w-6 items-center justify-center rounded-md bg-surface text-xs font-semibold text-text-muted">
          {initials}
        </span>
        <span className="hidden max-w-32 truncate lg:block">{user.fullName}</span>
        <ChevronDown className="h-4 w-4 text-text-subtle" />
      </DropdownTrigger>

      <DropdownContent>
        <div className="border-b border-border px-3 py-2.5">
          <div className="flex items-start gap-2">
            <UserCircle className="mt-0.5 h-4 w-4 shrink-0 text-text-subtle" />
            <div className="min-w-0">
              <p className="truncate text-sm font-medium text-text-main">{user.fullName}</p>
              <p className="truncate text-xs text-text-muted">{user.email}</p>
              <Badge variant="secondary" className="mt-2 capitalize">
                {user.role.toLowerCase()}
              </Badge>
            </div>
          </div>
        </div>

        <a className="flex items-center gap-2 px-3 py-2 text-sm text-text-main hover:bg-surface" href="/profile">
          <UserCircle className="h-4 w-4 text-text-subtle" />
          Profile
        </a>
        <DropdownItem
          className="text-error hover:bg-red-50"
          onSelect={() => {
            logout();
            window.location.href = '/login';
          }}
        >
          <LogOut className="h-4 w-4" />
          Sign out
        </DropdownItem>
      </DropdownContent>
    </Dropdown>
  );
}

import React from 'react';
import {
  Database,
  FileSearch,
  FileText,
  FolderOpen,
  LayoutDashboard,
  MessageSquare,
  Settings,
  Shield,
  Tags,
  UserCircle,
} from 'lucide-react';
import { cn } from '../lib/cn';
import { useAuth } from '../lib/auth';

const baseNavigation = [
  { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
  { name: 'Documents', href: '/documents', icon: FileText },
  { name: 'AI Assistant', href: '/ai-assistant', icon: MessageSquare },
  { name: 'Collections', href: '/collections', icon: FolderOpen },
  { name: 'Search', href: '/search', icon: FileSearch },
  { name: 'Tags', href: '/tags', icon: Tags },
  { name: 'Profile', href: '/profile', icon: UserCircle },
  { name: 'Settings', href: '/settings', icon: Settings },
] as const;

const adminNavigation = [
  { name: 'Admin', href: '/admin', icon: Shield },
] as const;

interface SidebarProps {
  currentPath: string;
  onNavigate?: () => void;
  className?: string;
}

export function Sidebar({ currentPath, onNavigate, className = '' }: SidebarProps) {
  const auth = useAuth();
  const isAdmin = auth.user?.role === 'ADMIN';

  const navigation = isAdmin
    ? [...baseNavigation, ...adminNavigation]
    : baseNavigation;

  return (
    <aside className={cn('flex h-full flex-col border-r border-border bg-surface', className)}>
      <div className="flex h-14 shrink-0 items-center gap-3 border-b border-border px-4">
        <div className="flex h-8 w-8 items-center justify-center rounded-md border border-border bg-panel">
          <Database className="h-4 w-4 text-primary" />
        </div>
        <div className="min-w-0">
          <div className="truncate text-sm font-semibold text-text-main">Knowledge Base</div>
          <div className="truncate text-xs text-text-muted">Workspace</div>
        </div>
      </div>

      <nav className="flex-1 overflow-y-auto px-3 py-3" aria-label="Primary navigation">
        <ul className="space-y-0.5">
          {navigation.map((item) => {
            const Icon = item.icon;
            const isActive = currentPath === item.href;

            return (
              <li key={item.name}>
                <a
                  href={item.href}
                  aria-current={isActive ? 'page' : undefined}
                  onClick={onNavigate}
                  className={cn(
                    'flex h-9 items-center gap-3 rounded-md px-3 text-sm font-medium transition-colors',
                    isActive
                      ? 'bg-panel text-text-main shadow-sm ring-1 ring-border'
                      : 'text-text-muted hover:bg-panel hover:text-text-main',
                    item.name === 'Admin' && !isActive && 'text-red-500 hover:text-red-600 hover:bg-red-50/50'
                  )}
                >
                  <Icon className="h-4 w-4 shrink-0" />
                  <span className="truncate">{item.name}</span>
                </a>
              </li>
            );
          })}
        </ul>
      </nav>

      <div className="border-t border-border px-4 py-3">
        {auth.user && (
          <div className="flex items-center gap-2 mb-2">
            <div className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 text-[10px] font-bold text-white">
              {auth.user.fullName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)}
            </div>
            <div className="min-w-0">
              <div className="truncate text-xs font-medium text-text-main">{auth.user.fullName}</div>
              <div className="truncate text-[10px] text-text-muted">{auth.user.role}</div>
            </div>
          </div>
        )}
        <div className="text-xs font-medium uppercase tracking-wide text-text-subtle">Phase 1</div>
        <div className="mt-1 text-xs text-text-muted">Authentication and workspace shell</div>
      </div>
    </aside>
  );
}

import React from 'react';
import { Bell, Menu, Search, X } from 'lucide-react';
import { Input } from './ui/Input';
import { ThemeToggle } from './ui/ThemeToggle';
import { UserMenu } from './UserMenu';
import { Sidebar } from './Sidebar';
import { getNavigationLabel } from '../lib/navigation';

interface TopNavProps {
  currentPath: string;
}

export function TopNav({ currentPath }: TopNavProps) {
  const [mobileMenuOpen, setMobileMenuOpen] = React.useState(false);
  const label = getNavigationLabel(currentPath);

  React.useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setMobileMenuOpen(false);
      }
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, []);

  return (
    <>
      <header className="fixed left-0 right-0 top-0 z-40 flex h-14 items-center border-b border-border bg-panel px-3 md:left-64 md:px-4">
        <div className="flex min-w-0 flex-1 items-center gap-3">
          <button
            type="button"
            className="inline-flex h-9 w-9 items-center justify-center rounded-md border border-border bg-panel text-text-muted hover:bg-surface hover:text-text-main md:hidden"
            onClick={() => setMobileMenuOpen(true)}
          >
            <Menu className="h-4 w-4" />
            <span className="sr-only">Open navigation</span>
          </button>

          <div className="hidden min-w-0 items-center gap-2 text-sm md:flex">
            <span className="text-text-muted">Knowledge Base</span>
            <span className="text-text-subtle">/</span>
            <span className="truncate font-medium text-text-main">{label}</span>
          </div>

          <div className="min-w-0 text-sm font-semibold text-text-main md:hidden">{label}</div>
        </div>

        <div className="flex items-center gap-2">
          <form 
            className="relative hidden sm:block" 
            onSubmit={(e) => {
              e.preventDefault();
              const q = (e.target as HTMLFormElement).search.value;
              if (q) window.location.href = `/search?q=${encodeURIComponent(q)}`;
            }}
          >
            <Search className="pointer-events-none absolute left-2.5 top-2.5 h-4 w-4 text-text-subtle" />
            <Input
              name="search"
              type="search"
              placeholder="Search knowledge"
              className="h-9 w-56 bg-surface pl-9 lg:w-72"
              aria-label="Search knowledge"
            />
          </form>

          <button
            type="button"
            className="relative inline-flex h-9 w-9 items-center justify-center rounded-md border border-transparent text-text-muted hover:border-border hover:bg-surface hover:text-text-main focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-border-focus"
          >
            <Bell className="h-4 w-4" />
            <span className="sr-only">Notifications</span>
            <span className="absolute right-2 top-2 h-1.5 w-1.5 rounded-full bg-error" />
          </button>

          <ThemeToggle />
          <UserMenu />
        </div>
      </header>

      {mobileMenuOpen && (
        <div className="fixed inset-0 z-50 md:hidden">
          <div className="absolute inset-0 bg-black/30" onClick={() => setMobileMenuOpen(false)} />
          <div className="absolute inset-y-0 left-0 flex w-72 max-w-[85vw] flex-col bg-surface shadow-xl">
            <div className="absolute right-3 top-3 z-10">
              <button
                type="button"
                className="inline-flex h-8 w-8 items-center justify-center rounded-md text-text-muted hover:bg-panel hover:text-text-main"
                onClick={() => setMobileMenuOpen(false)}
              >
                <X className="h-4 w-4" />
                <span className="sr-only">Close navigation</span>
              </button>
            </div>
            <Sidebar currentPath={currentPath} onNavigate={() => setMobileMenuOpen(false)} />
          </div>
        </div>
      )}
    </>
  );
}

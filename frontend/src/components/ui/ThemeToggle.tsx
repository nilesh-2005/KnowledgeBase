import React, { useEffect, useState } from 'react';
import { Sun, Moon } from 'lucide-react';

export function ThemeToggle() {
  const [isDark, setIsDark] = useState(() => {
    if (typeof document !== 'undefined') {
      return document.documentElement.classList.contains('dark');
    }
    return false;
  });

  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [isDark]);

  return (
    <button
      type="button"
      aria-label="Toggle dark mode"
      className="inline-flex h-9 w-9 items-center justify-center rounded-md border border-transparent text-text-muted hover:border-border hover:bg-surface hover:text-text-main focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-border-focus"
      onClick={() => setIsDark(!isDark)}
    >
      {isDark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
    </button>
  );
}

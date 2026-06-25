import React from 'react';
import { cn } from '../../lib/cn';

interface DropdownContextValue {
  open: boolean;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
}

const DropdownContext = React.createContext<DropdownContextValue | null>(null);

function useDropdown() {
  const context = React.useContext(DropdownContext);
  if (!context) {
    throw new Error('Dropdown components must be used inside Dropdown');
  }
  return context;
}

export function Dropdown({ children }: { children: React.ReactNode }) {
  const [open, setOpen] = React.useState(false);
  const rootRef = React.useRef<HTMLDivElement>(null);

  React.useEffect(() => {
    function handlePointerDown(event: PointerEvent) {
      if (!rootRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setOpen(false);
      }
    }

    document.addEventListener('pointerdown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('pointerdown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, []);

  return (
    <DropdownContext.Provider value={{ open, setOpen }}>
      <div ref={rootRef} className="relative">
        {children}
      </div>
    </DropdownContext.Provider>
  );
}

export function DropdownTrigger({
  children,
  className = '',
  onClick,
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement>) {
  const { open, setOpen } = useDropdown();

  return (
    <button
      type="button"
      aria-haspopup="menu"
      aria-expanded={open}
      className={className}
      {...props}
      onClick={(event) => {
        onClick?.(event);
        setOpen((current) => !current);
      }}
    >
      {children}
    </button>
  );
}

export function DropdownContent({
  children,
  className = '',
}: {
  children: React.ReactNode;
  className?: string;
}) {
  const { open } = useDropdown();

  if (!open) {
    return null;
  }

  return (
    <div
      role="menu"
      className={cn(
        'absolute right-0 z-50 mt-2 min-w-56 rounded-md border border-border bg-panel py-1 shadow-md',
        className
      )}
    >
      {children}
    </div>
  );
}

export function DropdownItem({
  children,
  className = '',
  onClick,
  onSelect,
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & { onSelect?: () => void }) {
  const { setOpen } = useDropdown();

  return (
    <button
      type="button"
      role="menuitem"
      className={cn('flex w-full items-center gap-2 px-3 py-2 text-left text-sm text-text-main hover:bg-surface', className)}
      {...props}
      onClick={(event) => {
        onClick?.(event);
        onSelect?.();
        setOpen(false);
      }}
    >
      {children}
    </button>
  );
}

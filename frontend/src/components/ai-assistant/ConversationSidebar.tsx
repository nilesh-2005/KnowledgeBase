import React from 'react';
import type { Conversation } from '../../lib/api';

interface ConversationSidebarProps {
  conversations: Conversation[];
  activeId: string | null;
  onSelect: (id: string) => void;
  onNew: () => void;
}

export function ConversationSidebar({ conversations, activeId, onSelect, onNew }: ConversationSidebarProps) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  const grouped = conversations.reduce(
    (acc, conv) => {
      const d = new Date(conv.updatedAt);
      if (d >= today) acc.today.push(conv);
      else if (d >= yesterday) acc.yesterday.push(conv);
      else acc.earlier.push(conv);
      return acc;
    },
    { today: [] as Conversation[], yesterday: [] as Conversation[], earlier: [] as Conversation[] }
  );

  const renderGroup = (label: string, list: Conversation[]) => {
    if (list.length === 0) return null;
    return (
      <div className="mb-4">
        <h3 className="text-[11px] font-semibold uppercase tracking-wider text-text-subtle mb-2 px-2">{label}</h3>
        <ul className="space-y-0.5">
          {list.map(conv => (
            <li key={conv.id}>
              <button
                onClick={() => onSelect(conv.id)}
                className={`w-full text-left truncate px-2 py-1.5 text-sm rounded transition-colors ${
                  activeId === conv.id ? 'bg-surface text-text-main font-medium' : 'text-text-muted hover:bg-border/30 hover:text-text-main'
                }`}
              >
                {conv.title}
              </button>
            </li>
          ))}
        </ul>
      </div>
    );
  };

  return (
    <div className="flex h-full w-64 shrink-0 flex-col border-r border-border bg-panel">
      <div className="p-3 border-b border-border">
        <button
          onClick={onNew}
          className="flex w-full items-center gap-2 justify-center rounded bg-primary px-3 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-hover focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-panel"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          New Chat
        </button>
      </div>
      <div className="flex-1 overflow-y-auto p-2">
        {renderGroup('Today', grouped.today)}
        {renderGroup('Yesterday', grouped.yesterday)}
        {renderGroup('Earlier', grouped.earlier)}
      </div>
    </div>
  );
}

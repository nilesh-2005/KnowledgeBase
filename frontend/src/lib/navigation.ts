import {
  FileSearch,
  FileText,
  FolderOpen,
  LayoutDashboard,
  MessageSquare,
  Settings,
  Tags,
  UserCircle,
} from 'lucide-react';

export const appNavigation = [
  { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
  { name: 'Documents', href: '/documents', icon: FileText },
  { name: 'AI Assistant', href: '/ai-assistant', icon: MessageSquare },
  { name: 'Collections', href: '/collections', icon: FolderOpen },
  { name: 'Search', href: '/search', icon: FileSearch },
  { name: 'Tags', href: '/tags', icon: Tags },
  { name: 'Profile', href: '/profile', icon: UserCircle },
  { name: 'Settings', href: '/settings', icon: Settings },
] as const;

export function getNavigationLabel(pathname: string) {
  return appNavigation.find((item) => item.href === pathname)?.name ?? 'Knowledge Base';
}

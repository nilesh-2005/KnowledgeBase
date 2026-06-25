import React, { useEffect, useState } from 'react';
import { apiClient } from '../../lib/api';
import { FileText, Tags, Share2, HardDrive } from 'lucide-react';

interface StatItem {
  label: string;
  value: string;
  description: string;
  icon: React.FC<{ className?: string }>;
}

export const DashboardStats: React.FC = () => {
  const [stats, setStats] = useState<StatItem[]>([
    { label: 'Documents', value: '—', description: 'Loading...', icon: FileText },
    { label: 'Tags', value: '—', description: 'Loading...', icon: Tags },
    { label: 'Shared Items', value: '—', description: 'Loading...', icon: Share2 },
    { label: 'Storage Usage', value: '—', description: 'Loading...', icon: HardDrive },
  ]);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      // Fetch documents (paged — we just need totalElements)
      const docsRes = await apiClient.getDocuments(0, 1);
      const docCount = docsRes?.totalElements ?? 0;

      // Fetch tags (returns plain array)
      const tagsRes = await apiClient.getTags();
      const tagCount = Array.isArray(tagsRes) ? tagsRes.length : 0;

      // Fetch collections (paged)
      const collectionsRes = await apiClient.getCollections(0, 1);
      const collectionCount = collectionsRes?.totalElements ?? 0;

      // Compute approximate storage from documents
      let totalBytes = 0;
      if (docCount > 0) {
        // Fetch a larger page to sum up file sizes
        const allDocs = await apiClient.getDocuments(0, 100);
        totalBytes = (allDocs?.content ?? []).reduce((sum: number, doc: any) => sum + (doc.fileSize || 0), 0);
      }

      const storageMB = (totalBytes / (1024 * 1024)).toFixed(2);
      const storageDisplay = totalBytes < 1024 * 1024
        ? `${(totalBytes / 1024).toFixed(0)} KB`
        : `${storageMB} MB`;

      setStats([
        {
          label: 'Documents',
          value: String(docCount),
          description: docCount === 0 ? 'No documents uploaded yet' : `${docCount} document${docCount > 1 ? 's' : ''} uploaded`,
          icon: FileText,
        },
        {
          label: 'Tags',
          value: String(tagCount),
          description: tagCount === 0 ? 'No taxonomy configured' : `${tagCount} tag${tagCount > 1 ? 's' : ''} active`,
          icon: Tags,
        },
        {
          label: 'Collections',
          value: String(collectionCount),
          description: collectionCount === 0 ? 'No collections created' : `${collectionCount} collection${collectionCount > 1 ? 's' : ''} active`,
          icon: Share2,
        },
        {
          label: 'Storage Usage',
          value: totalBytes === 0 ? '0 MB' : storageDisplay,
          description: '5 GB available',
          icon: HardDrive,
        },
      ]);
    } catch (err) {
      console.error('Failed to fetch dashboard stats:', err);
      // Keep placeholder values on error
    }
  };

  return (
    <section aria-label="Knowledge base statistics" className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
      {stats.map((stat) => {
        const Icon = stat.icon;
        return (
          <div key={stat.label} className="rounded-lg border border-border bg-panel shadow-sm">
            <div className="p-4">
              <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                  <p className="text-sm font-medium text-text-muted">{stat.label}</p>
                  <p className="mt-2 text-2xl font-semibold leading-8 text-text-main">{stat.value}</p>
                  <p className="mt-1 truncate text-xs text-text-muted">{stat.description}</p>
                </div>
                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-md border border-border bg-surface">
                  <Icon className="h-4 w-4 text-text-muted" />
                </div>
              </div>
            </div>
          </div>
        );
      })}
    </section>
  );
};

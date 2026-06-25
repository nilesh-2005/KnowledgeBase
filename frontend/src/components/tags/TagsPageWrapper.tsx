import React, { useEffect, useState } from 'react';
import { apiClient, type TagDto } from '../../lib/api';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../ui/Table';

const TAG_COLORS = [
  { name: 'Blue', value: '#3B82F6' },
  { name: 'Green', value: '#22C55E' },
  { name: 'Red', value: '#EF4444' },
  { name: 'Purple', value: '#A855F7' },
  { name: 'Orange', value: '#F97316' },
  { name: 'Teal', value: '#14B8A6' },
  { name: 'Pink', value: '#EC4899' },
  { name: 'Yellow', value: '#EAB308' },
  { name: 'Indigo', value: '#6366F1' },
  { name: 'Gray', value: '#6B7280' },
];

export const TagsPageWrapper: React.FC = () => {
  const [tags, setTags] = useState<TagDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [isCreating, setIsCreating] = useState(false);
  const [newTagName, setNewTagName] = useState('');
  const [newTagColor, setNewTagColor] = useState(TAG_COLORS[0].value);

  useEffect(() => {
    fetchTags();
  }, []);

  const fetchTags = async () => {
    try {
      setLoading(true);
      const data = await apiClient.getTags();
      setTags(Array.isArray(data) ? data : []);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to load tags');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTagName.trim()) return;

    try {
      setLoading(true);
      setError(null);
      await apiClient.createTag({ name: newTagName.trim(), color: newTagColor });
      setNewTagName('');
      setNewTagColor(TAG_COLORS[0].value);
      setIsCreating(false);
      await fetchTags();
    } catch (err: any) {
      setError(err.message || 'Failed to create tag');
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this tag?')) return;
    try {
      setLoading(true);
      await apiClient.deleteTag(id);
      await fetchTags();
    } catch (err: any) {
      alert(err.message || 'Failed to delete tag');
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-end">
        <button
          onClick={() => setIsCreating(!isCreating)}
          className="inline-flex h-9 items-center justify-center gap-2 rounded-md border border-primary bg-primary px-3 text-sm font-medium text-on-primary hover:bg-blue-700"
        >
          {isCreating ? 'Cancel' : '+ New Tag'}
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-md bg-red-50 text-red-700 text-sm">
          {error}
        </div>
      )}

      {isCreating && (
        <form onSubmit={handleCreate} className="bg-white p-4 border border-gray-200 rounded-md shadow-sm space-y-4">
          <div className="flex gap-3">
            <div className="flex-1">
              <label className="block text-sm font-medium text-gray-700 mb-1">Tag Name</label>
              <input
                type="text"
                placeholder="e.g. engineering, draft, urgent"
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                value={newTagName}
                onChange={(e) => setNewTagName(e.target.value)}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Color</label>
              <div className="flex gap-1.5 flex-wrap pt-1">
                {TAG_COLORS.map(c => (
                  <button
                    key={c.value}
                    type="button"
                    title={c.name}
                    onClick={() => setNewTagColor(c.value)}
                    className="w-7 h-7 rounded-full border-2 transition-all"
                    style={{
                      backgroundColor: c.value,
                      borderColor: newTagColor === c.value ? '#1e293b' : 'transparent',
                      transform: newTagColor === c.value ? 'scale(1.15)' : 'scale(1)',
                    }}
                  />
                ))}
              </div>
            </div>
          </div>
          <div className="flex justify-end">
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-primary text-white rounded-md text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
            >
              Save Tag
            </button>
          </div>
        </form>
      )}

      <div className="bg-white rounded-md border border-gray-200 shadow-sm">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Tag</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading && tags.length === 0 ? (
              <TableRow>
                <TableCell colSpan={2} className="text-center py-8">Loading...</TableCell>
              </TableRow>
            ) : tags.length === 0 ? (
              <TableRow>
                <TableCell colSpan={2} className="text-center py-8 text-gray-500">
                  No tags created yet. Click "+ New Tag" to get started.
                </TableCell>
              </TableRow>
            ) : (
              tags.map(tag => (
                <TableRow key={tag.id}>
                  <TableCell>
                    <span
                      className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium"
                      style={{
                        backgroundColor: `${tag.color || '#6366F1'}20`,
                        color: tag.color || '#6366F1',
                      }}
                    >
                      <span
                        className="w-2 h-2 rounded-full"
                        style={{ backgroundColor: tag.color || '#6366F1' }}
                      />
                      {tag.name}
                    </span>
                  </TableCell>
                  <TableCell className="text-right">
                    <button
                      onClick={() => handleDelete(tag.id!)}
                      className="text-red-600 hover:text-red-900 text-sm font-medium"
                    >
                      Delete
                    </button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};

import React, { useEffect, useState } from 'react';
import { apiClient, type CollectionDto } from '../../lib/api';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../ui/Table';

export const CollectionsPageWrapper: React.FC = () => {
  const [collections, setCollections] = useState<CollectionDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [isCreating, setIsCreating] = useState(false);
  const [newCollectionName, setNewCollectionName] = useState('');
  const [newCollectionDesc, setNewCollectionDesc] = useState('');

  useEffect(() => {
    fetchCollections();
  }, []);

  const fetchCollections = async () => {
    try {
      setLoading(true);
      const res = await apiClient.getCollections(0, 100);
      // Spring Page returns { content: [...], ... }
      setCollections(res.content ?? (Array.isArray(res) ? res : []));
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to load collections');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCollectionName.trim()) return;

    try {
      setLoading(true);
      setError(null);
      await apiClient.createCollection({ 
        name: newCollectionName.trim(),
        description: newCollectionDesc.trim() || undefined,
      });
      setNewCollectionName('');
      setNewCollectionDesc('');
      setIsCreating(false);
      await fetchCollections();
    } catch (err: any) {
      setError(err.message || 'Failed to create collection');
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this collection? Documents inside will lose their collection association.')) return;
    try {
      setLoading(true);
      await apiClient.deleteCollection(id);
      await fetchCollections();
    } catch (err: any) {
      alert(err.message || 'Failed to delete collection');
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
          {isCreating ? 'Cancel' : '+ New Collection'}
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-md bg-red-50 text-red-700 text-sm">
          {error}
        </div>
      )}

      {isCreating && (
        <form onSubmit={handleCreate} className="bg-white p-4 border border-gray-200 rounded-md shadow-sm space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Collection Name</label>
            <input
              type="text"
              required
              placeholder="e.g. Engineering Docs, Onboarding"
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
              value={newCollectionName}
              onChange={(e) => setNewCollectionName(e.target.value)}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Description (optional)</label>
            <textarea
              rows={2}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
              value={newCollectionDesc}
              onChange={(e) => setNewCollectionDesc(e.target.value)}
            />
          </div>
          <div className="flex justify-end">
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-primary text-white rounded-md text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
            >
              Save Collection
            </button>
          </div>
        </form>
      )}

      <div className="bg-white rounded-md border border-gray-200 shadow-sm">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Description</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading && collections.length === 0 ? (
              <TableRow>
                <TableCell colSpan={3} className="text-center py-8">Loading...</TableCell>
              </TableRow>
            ) : collections.length === 0 ? (
              <TableRow>
                <TableCell colSpan={3} className="text-center py-8 text-gray-500">
                  No collections created yet. Click "+ New Collection" to organize your documents.
                </TableCell>
              </TableRow>
            ) : (
              collections.map(collection => (
                <TableRow key={collection.id}>
                  <TableCell className="font-medium">{collection.name}</TableCell>
                  <TableCell className="text-gray-500">{collection.description || '—'}</TableCell>
                  <TableCell className="text-right">
                    <button
                      onClick={() => handleDelete(collection.id!)}
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

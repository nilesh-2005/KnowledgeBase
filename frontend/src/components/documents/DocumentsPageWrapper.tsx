import React, { useEffect, useState } from 'react';
import { apiClient, type DocumentResponse } from '../../lib/api';
import { DocumentTable } from './DocumentTable';

export const DocumentsPageWrapper: React.FC = () => {
  const [documents, setDocuments] = useState<DocumentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    fetchDocuments();
  }, []);

  const fetchDocuments = async () => {
    try {
      setLoading(true);
      const res = await apiClient.getDocuments(0, 100);
      setDocuments(res.content);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      if (searchQuery.trim()) {
        const res = await apiClient.searchDocuments(searchQuery, 0, 100);
        setDocuments(res.content);
      } else {
        await fetchDocuments();
      }
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to search documents');
    } finally {
      setLoading(false);
    }
  };

  const handleDocumentClick = (id: string) => {
    window.location.href = `/documents/${id}`;
  };

  return (
    <div className="space-y-4">
      <form onSubmit={handleSearch} className="flex gap-2">
        <input
          type="text"
          placeholder="Search documents by title, description, or tags..."
          className="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
        <button
          type="submit"
          className="px-4 py-2 bg-white border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          Search
        </button>
      </form>

      {error && (
        <div className="p-4 rounded-md bg-red-50 text-red-700 text-sm">
          {error}
        </div>
      )}

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
        </div>
      ) : (
        <DocumentTable documents={documents} onDocumentClick={handleDocumentClick} />
      )}
    </div>
  );
};

import React, { useEffect, useState } from 'react';
import { apiClient, type DocumentResponse } from '../../lib/api';
import { FileSearch, Search } from 'lucide-react';

export const SearchWrapper: React.FC = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<DocumentResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const q = params.get('q');
    if (q) {
      setQuery(q);
      performSearch(q);
    }
  }, []);

  const performSearch = async (q: string) => {
    if (!q.trim()) {
      setResults([]);
      setHasSearched(false);
      return;
    }
    try {
      setLoading(true);
      const res = await apiClient.searchDocuments(q, 0, 50);
      setResults(res.content || []);
      setHasSearched(true);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    window.history.replaceState(null, '', `?q=${encodeURIComponent(query)}`);
    performSearch(query);
  };

  return (
    <div>
      <form onSubmit={handleSubmit} className="relative">
        <Search className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-text-subtle" />
        <input 
          id="knowledge-search" 
          type="search" 
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50 pl-9" 
          placeholder="Search documents by title or description..." 
        />
      </form>

      <div className="mt-4">
        {loading ? (
           <div className="flex justify-center py-8">
             <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
           </div>
        ) : hasSearched && results.length === 0 ? (
          <div className="flex min-h-72 flex-col items-center justify-center rounded-md border border-dashed border-border bg-surface px-6 py-10 text-center">
            <div className="flex h-10 w-10 items-center justify-center rounded-md border border-border bg-panel">
              <FileSearch className="h-5 w-5 text-text-muted" />
            </div>
            <h2 className="mt-3 text-sm font-semibold text-text-main">No results found</h2>
            <p className="mt-1 max-w-md text-sm text-text-muted">
              We couldn't find anything matching "{query}".
            </p>
          </div>
        ) : hasSearched && results.length > 0 ? (
          <ul className="divide-y divide-gray-200 border rounded-md">
            {results.map(doc => (
              <li key={doc.id} className="p-4 hover:bg-gray-50 flex justify-between items-center">
                <div>
                  <a href={`/documents/${doc.id}`} className="text-sm font-medium text-indigo-600 hover:text-indigo-900">{doc.title}</a>
                  <p className="text-sm text-gray-500">{doc.description}</p>
                </div>
                <div className="flex gap-2">
                  {doc.tags?.map(tag => (
                     <span key={tag.id} className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                       <span className="w-2 h-2 rounded-full" style={{ backgroundColor: tag.color || '#6366F1' }} />
                       {tag.name}
                     </span>
                  ))}
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <div className="flex min-h-72 flex-col items-center justify-center rounded-md border border-dashed border-border bg-surface px-6 py-10 text-center">
            <div className="flex h-10 w-10 items-center justify-center rounded-md border border-border bg-panel">
              <Search className="h-5 w-5 text-text-muted" />
            </div>
            <h2 className="mt-3 text-sm font-semibold text-text-main">Search Knowledge</h2>
            <p className="mt-1 max-w-md text-sm text-text-muted">
              Enter a keyword above to search through documents.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

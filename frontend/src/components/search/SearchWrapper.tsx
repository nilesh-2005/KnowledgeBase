import React, { useEffect, useState } from 'react';
import { apiClient, type ChunkSearchResult } from '../../lib/api';
import { FileSearch, Search, ChevronRight } from 'lucide-react';

interface GroupedResult {
  documentId: string;
  documentTitle: string;
  chunks: ChunkSearchResult[];
}

export const SearchWrapper: React.FC = () => {
  const [query, setQuery] = useState('');
  const [groupedResults, setGroupedResults] = useState<GroupedResult[]>([]);
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
      setGroupedResults([]);
      setHasSearched(false);
      return;
    }
    try {
      setLoading(true);
      const res = await apiClient.searchChunks(q, 50);
      
      const groups = new Map<string, GroupedResult>();
      res.forEach(chunk => {
        if (!groups.has(chunk.documentId)) {
          groups.set(chunk.documentId, {
            documentId: chunk.documentId,
            documentTitle: chunk.documentTitle,
            chunks: []
          });
        }
        groups.get(chunk.documentId)!.chunks.push(chunk);
      });
      
      setGroupedResults(Array.from(groups.values()));
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
          placeholder="Search through documents..." 
        />
      </form>

      <div className="mt-4">
        {loading ? (
           <div className="flex justify-center py-8">
             <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
           </div>
        ) : hasSearched && groupedResults.length === 0 ? (
          <div className="flex min-h-72 flex-col items-center justify-center rounded-md border border-dashed border-border bg-surface px-6 py-10 text-center">
            <div className="flex h-10 w-10 items-center justify-center rounded-md border border-border bg-panel">
              <FileSearch className="h-5 w-5 text-text-muted" />
            </div>
            <h2 className="mt-3 text-sm font-semibold text-text-main">No results found</h2>
            <p className="mt-1 max-w-md text-sm text-text-muted">
              We couldn't find anything matching "{query}".
            </p>
          </div>
        ) : hasSearched && groupedResults.length > 0 ? (
          <ul className="space-y-4">
            {groupedResults.map(group => (
              <li key={group.documentId} className="border rounded-md bg-white overflow-hidden shadow-sm">
                <div className="p-4 border-b bg-gray-50 flex items-center justify-between">
                  <a href={`/documents/${group.documentId}`} className="text-sm font-medium text-indigo-600 hover:text-indigo-900 flex items-center gap-1">
                    {group.documentTitle}
                    <ChevronRight className="h-4 w-4" />
                  </a>
                  <span className="text-xs text-gray-500">{group.chunks.length} matches</span>
                </div>
                <div className="divide-y divide-gray-100">
                  {group.chunks.slice(0, 3).map(chunk => (
                    <a href={`/documents/${group.documentId}?chunk=${chunk.chunkIndex}`} key={`${group.documentId}-${chunk.chunkIndex}`} className="block p-4 hover:bg-gray-50 transition-colors">
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Chunk {chunk.chunkIndex}</span>
                        <span className="text-xs bg-gray-100 px-2 py-0.5 rounded text-gray-600">Score: {Math.round(chunk.score * 100)}%</span>
                      </div>
                      <p className="text-sm text-gray-600 line-clamp-2 italic">"{chunk.content}"</p>
                    </a>
                  ))}
                  {group.chunks.length > 3 && (
                     <div className="p-3 text-center bg-gray-50 border-t">
                       <a href={`/documents/${group.documentId}`} className="text-xs text-indigo-600 hover:text-indigo-800 font-medium">
                         View {group.chunks.length - 3} more matches in document
                       </a>
                     </div>
                  )}
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

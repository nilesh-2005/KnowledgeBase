import React, { useEffect, useState, useRef } from 'react';
import { apiClient, type DocumentResponse, type DocumentChunkResponse } from '../lib/api';
import { useAuth } from '../lib/auth';
import { FileText, Download, Trash2, Zap, MoreVertical } from 'lucide-react';

interface DocumentViewerProps {
  documentId: string;
}

export const DocumentViewer: React.FC<DocumentViewerProps> = ({ documentId }) => {
  const [doc, setDoc] = useState<DocumentResponse | null>(null);
  const [chunks, setChunks] = useState<DocumentChunkResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const auth = useAuth();
  
  const contentRef = useRef<HTMLDivElement>(null);

  const canDelete = auth.user?.role === 'ADMIN' || auth.user?.role === 'EMPLOYEE';

  useEffect(() => {
    fetchData();
  }, [documentId]);

  useEffect(() => {
    if (!loading && chunks.length > 0) {
      // Check for ?chunk= index in URL
      const params = new URLSearchParams(window.location.search);
      const chunkParam = params.get('chunk');
      if (chunkParam) {
        scrollToChunk(parseInt(chunkParam, 10));
      }
    }
  }, [loading, chunks]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [docRes, chunksRes] = await Promise.all([
        apiClient.getDocument(documentId),
        apiClient.getDocumentChunks(documentId)
      ]);
      setDoc(docRes);
      setChunks(chunksRes);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to load document data');
    } finally {
      setLoading(false);
    }
  };

  const scrollToChunk = (chunkIndex: number) => {
    setTimeout(() => {
      const element = document.querySelector(`[data-chunk-index="${chunkIndex}"]`);
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center' });
        
        // Brief flash/highlight effect
        element.classList.add('bg-yellow-100', 'transition-colors', 'duration-500');
        setTimeout(() => {
          element.classList.remove('bg-yellow-100');
        }, 2000);
      }
    }, 100);
  };

  const handleDownload = () => {
    const token = localStorage.getItem('authToken');
    fetch(`http://localhost:8080/api/documents/${documentId}/download`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error('Download failed');
        return res.blob();
      })
      .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = window.document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = doc?.fileName ?? 'download';
        window.document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        window.document.body.removeChild(a);
      })
      .catch(() => alert('Failed to download document'));
  };

  const handleDelete = async () => {
    if (!canDelete) return;
    if (confirm('Are you sure you want to delete this document? This cannot be undone.')) {
      try {
        await apiClient.deleteDocument(documentId);
        window.location.href = '/documents';
      } catch (err: any) {
        alert(err.message || 'Failed to delete document');
      }
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  if (error || !doc) {
    return (
      <div className="p-4 rounded-md bg-red-50 text-red-700">
        {error || 'Document not found'}
      </div>
    );
  }

  const visibilityStyles: Record<string, string> = {
    PUBLIC: 'bg-green-100 text-green-800',
    TEAM: 'bg-purple-100 text-purple-800',
    PRIVATE: 'bg-gray-100 text-gray-800',
  };

  return (
    <div className="flex flex-col lg:flex-row gap-6 h-[calc(100vh-8rem)]">
      {/* Sidebar: Metadata */}
      <div className="w-full lg:w-80 flex-shrink-0 bg-white border border-border rounded-lg shadow-sm flex flex-col h-full overflow-y-auto">
        <div className="p-4 border-b border-border bg-panel sticky top-0">
          <h3 className="font-semibold text-text-main flex items-center gap-2">
            <FileText className="h-4 w-4 text-indigo-600" />
            Metadata
          </h3>
        </div>
        <div className="p-4 space-y-4 text-sm">
          <div>
            <span className="block text-text-muted font-medium mb-1">Title</span>
            <span className="text-text-main font-medium">{doc.title}</span>
          </div>
          <div>
            <span className="block text-text-muted font-medium mb-1">File Name</span>
            <span className="text-text-main break-all">{doc.fileName}</span>
          </div>
          <div>
            <span className="block text-text-muted font-medium mb-1">Visibility</span>
            <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${visibilityStyles[doc.visibility] ?? ''}`}>
              {doc.visibility}
            </span>
          </div>
          <div>
            <span className="block text-text-muted font-medium mb-1">Type & Size</span>
            <span className="text-text-main">{doc.fileType} • {(doc.fileSize / 1024 / 1024).toFixed(2)} MB</span>
          </div>
          <div>
            <span className="block text-text-muted font-medium mb-1">Collection</span>
            <span className="text-text-main">{doc.collection ? doc.collection.name : 'None'}</span>
          </div>
          <div>
            <span className="block text-text-muted font-medium mb-1">Tags</span>
            {doc.tags && doc.tags.length > 0 ? (
              <div className="flex flex-wrap gap-1.5 mt-1">
                {doc.tags.map(tag => (
                  <span
                    key={tag.id}
                    className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800"
                  >
                    <span className="w-1.5 h-1.5 rounded-full mr-1.5" style={{ backgroundColor: tag.color || '#6366F1' }} />
                    {tag.name}
                  </span>
                ))}
              </div>
            ) : <span className="text-text-main">None</span>}
          </div>
          <div>
            <span className="block text-text-muted font-medium mb-1">Uploaded At</span>
            <span className="text-text-main">{new Date(doc.createdAt).toLocaleDateString()}</span>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 bg-white border border-border rounded-lg shadow-sm h-full">
        {/* Toolbar */}
        <div className="px-4 py-3 border-b border-border bg-panel flex items-center justify-between sticky top-0 z-10">
          <h2 className="font-semibold text-text-main truncate pr-4">{doc.title}</h2>
          <div className="flex items-center gap-2 flex-shrink-0">
            <button
              onClick={handleDownload}
              className="inline-flex h-8 items-center justify-center gap-2 rounded-md border border-border bg-white px-3 text-xs font-medium text-text-main hover:bg-surface shadow-sm"
            >
              <Download className="h-3.5 w-3.5" />
              Download
            </button>
            {canDelete && (
              <button
                onClick={handleDelete}
                className="inline-flex h-8 items-center justify-center gap-2 rounded-md border border-transparent bg-red-600 px-3 text-xs font-medium text-white hover:bg-red-700 shadow-sm"
              >
                <Trash2 className="h-3.5 w-3.5" />
                Delete
              </button>
            )}
          </div>
        </div>

        {/* Document Content (Chunks) */}
        <div className="flex-1 overflow-y-auto p-6" ref={contentRef}>
          {chunks.length === 0 ? (
             <div className="text-center py-12 text-text-muted">
               No text chunks extracted.
             </div>
          ) : (
            <div className="max-w-3xl mx-auto space-y-6">
              {chunks.map((chunk) => (
                <div 
                  key={chunk.id} 
                  id={`chunk-${chunk.chunkIndex}`}
                  data-chunk-index={chunk.chunkIndex}
                  className="group relative p-4 -mx-4 rounded-lg hover:bg-gray-50 border border-transparent hover:border-gray-100 transition-colors"
                >
                  <div className="absolute -left-2 top-4 w-1 h-0 bg-indigo-500 rounded-r opacity-0 group-hover:h-full group-hover:opacity-100 transition-all"></div>
                  
                  <div className="flex items-center justify-between mb-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <span className="text-xs font-medium text-gray-400 uppercase tracking-wider">Chunk {chunk.chunkIndex}</span>
                    <div className="flex gap-1">
                       <button className="p-1 rounded hover:bg-gray-200 text-gray-500" title="Future Action: Summarize">
                         <Zap className="h-3 w-3" />
                       </button>
                       <button className="p-1 rounded hover:bg-gray-200 text-gray-500" title="More Actions">
                         <MoreVertical className="h-3 w-3" />
                       </button>
                    </div>
                  </div>
                  
                  <div className="prose prose-sm max-w-none text-gray-700 leading-relaxed whitespace-pre-wrap">
                    {chunk.content}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
      
      {/* Future AI Actions Sidebar (Placeholder) */}
      <div className="hidden xl:block w-64 flex-shrink-0 border border-dashed border-gray-300 rounded-lg bg-gray-50 flex flex-col items-center justify-center text-center p-4">
        <Zap className="h-8 w-8 text-gray-400 mb-2" />
        <h4 className="text-sm font-medium text-gray-600">AI Assistant</h4>
        <p className="text-xs text-gray-500 mt-1">Select text or chunks to explain, summarize, or take notes.</p>
      </div>
    </div>
  );
};

import React, { useEffect, useState } from 'react';
import { apiClient, type DocumentResponse } from '../../lib/api';
import { useAuth } from '../../lib/auth';

interface DocumentDetailWrapperProps {
  documentId: string;
}

export const DocumentDetailWrapper: React.FC<DocumentDetailWrapperProps> = ({ documentId }) => {
  const [doc, setDoc] = useState<DocumentResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const auth = useAuth();

  // Derive permission flags from user role
  const canDelete = auth.user?.role === 'ADMIN' || auth.user?.role === 'EMPLOYEE';

  useEffect(() => {
    fetchDocument();
  }, [documentId]);

  const fetchDocument = async () => {
    try {
      setLoading(true);
      const res = await apiClient.getDocument(documentId);
      setDoc(res);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to load document');
    } finally {
      setLoading(false);
    }
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
    <div className="bg-white shadow overflow-hidden sm:rounded-lg">
      <div className="px-4 py-5 sm:px-6 flex justify-between items-center">
        <div>
          <h3 className="text-lg leading-6 font-medium text-gray-900">{doc.title}</h3>
          <p className="mt-1 max-w-2xl text-sm text-gray-500">Document details and metadata.</p>
        </div>
        <div className="flex gap-3">
          {/* Download — available to all roles */}
          <button
            type="button"
            onClick={handleDownload}
            className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            Download
          </button>

          {/* Delete — hidden for VIEWER */}
          {canDelete && (
            <button
              type="button"
              onClick={handleDelete}
              className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
            >
              Delete
            </button>
          )}
        </div>
      </div>
      <div className="border-t border-gray-200">
        <dl>
          <div className="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt className="text-sm font-medium text-gray-500">File name</dt>
            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{doc.fileName}</dd>
          </div>
          <div className="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt className="text-sm font-medium text-gray-500">Description</dt>
            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{doc.description || 'No description provided.'}</dd>
          </div>
          <div className="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt className="text-sm font-medium text-gray-500">Visibility</dt>
            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${visibilityStyles[doc.visibility] ?? ''}`}>
                {doc.visibility}
              </span>
            </dd>
          </div>
          <div className="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt className="text-sm font-medium text-gray-500">File type & size</dt>
            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              {doc.fileType} — {(doc.fileSize / 1024 / 1024).toFixed(2)} MB
            </dd>
          </div>
          <div className="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt className="text-sm font-medium text-gray-500">Collection</dt>
            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              {doc.collection ? doc.collection.name : 'None'}
            </dd>
          </div>
          <div className="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt className="text-sm font-medium text-gray-500">Tags</dt>
            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              {doc.tags && doc.tags.length > 0 ? (
                <div className="flex flex-wrap gap-2">
                  {doc.tags.map(tag => (
                    <span
                      key={tag.id}
                      className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                      style={{ backgroundColor: `${tag.color}22`, color: tag.color, border: `1px solid ${tag.color}44` }}
                    >
                      {tag.name}
                    </span>
                  ))}
                </div>
              ) : 'None'}
            </dd>
          </div>
          <div className="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt className="text-sm font-medium text-gray-500">Uploaded by</dt>
            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              {doc.ownerId === auth.user?.id ? 'You' : doc.ownerId}
            </dd>
          </div>
          <div className="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt className="text-sm font-medium text-gray-500">Uploaded at</dt>
            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              {new Date(doc.createdAt).toLocaleString()}
            </dd>
          </div>
        </dl>
      </div>
    </div>
  );
};

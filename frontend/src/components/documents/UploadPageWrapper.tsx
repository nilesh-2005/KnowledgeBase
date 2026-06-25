import React, { useState } from 'react';
import { apiClient } from '../../lib/api';
import { UploadDropzone } from './UploadDropzone';
import { MetadataForm } from './MetadataForm';

export const UploadPageWrapper: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (metadata: any) => {
    if (!file) {
      setError('Please select a file to upload');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await apiClient.uploadDocument(file, metadata);
      // Redirect back to documents list
      window.location.href = '/documents';
    } catch (err: any) {
      setError(err.message || 'Failed to upload document');
      setLoading(false);
    }
  };

  return (
    <div className="bg-white shadow rounded-lg p-6 max-w-3xl mx-auto">
      <div className="mb-6">
        <h2 className="text-lg font-medium text-gray-900 mb-2">1. Select File</h2>
        <UploadDropzone selectedFile={file} onFileSelect={setFile} />
      </div>

      <div className="mt-8 pt-8 border-t border-gray-200">
        <h2 className="text-lg font-medium text-gray-900 mb-4">2. Document Details</h2>
        {error && (
          <div className="mb-4 p-3 bg-red-50 text-red-700 text-sm rounded-md">
            {error}
          </div>
        )}
        <MetadataForm onSubmit={handleSubmit} isLoading={loading} />
      </div>
    </div>
  );
};

import React from 'react';
import type { DocumentResponse } from '../../lib/api';

interface DocumentTableProps {
  documents: DocumentResponse[];
  onDocumentClick: (id: string) => void;
}

const formatBytes = (bytes: number, decimals = 2) => {
  if (!+bytes) return '0 Bytes';
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
};

export const DocumentTable: React.FC<DocumentTableProps> = ({ documents, onDocumentClick }) => {
  return (
    <div className="w-full overflow-x-auto rounded-lg border border-gray-200 shadow-sm">
      <table className="w-full text-sm text-left text-gray-500">
        <thead className="text-xs text-gray-700 uppercase bg-gray-50 border-b border-gray-200">
          <tr>
            <th scope="col" className="px-6 py-3 font-medium">Name</th>
            <th scope="col" className="px-6 py-3 font-medium">Type</th>
            <th scope="col" className="px-6 py-3 font-medium">Size</th>
            <th scope="col" className="px-6 py-3 font-medium">Visibility</th>
            <th scope="col" className="px-6 py-3 font-medium">Created Date</th>
          </tr>
        </thead>
        <tbody>
          {documents.length === 0 ? (
            <tr>
              <td colSpan={5} className="px-6 py-8 text-center text-gray-500">
                No documents found.
              </td>
            </tr>
          ) : (
            documents.map((doc) => (
              <tr 
                key={doc.id} 
                className="bg-white border-b hover:bg-gray-50 cursor-pointer transition-colors"
                onClick={() => onDocumentClick(doc.id)}
              >
                <td className="px-6 py-4 font-medium text-gray-900 whitespace-nowrap">
                  <div className="flex flex-col">
                    <span>{doc.title}</span>
                    <span className="text-xs text-gray-400 font-normal">{doc.fileName}</span>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                    {doc.fileType.split('/')[1] || doc.fileType}
                  </span>
                </td>
                <td className="px-6 py-4">{formatBytes(doc.fileSize)}</td>
                <td className="px-6 py-4">
                  <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium 
                    ${doc.visibility === 'PUBLIC' ? 'bg-green-100 text-green-800' : 
                      doc.visibility === 'TEAM' ? 'bg-purple-100 text-purple-800' : 
                      'bg-gray-100 text-gray-800'}`}>
                    {doc.visibility}
                  </span>
                </td>
                <td className="px-6 py-4">
                  {new Date(doc.createdAt).toLocaleDateString()}
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};

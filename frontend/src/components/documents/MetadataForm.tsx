import React, { useState, useEffect } from 'react';
import { apiClient, type TagDto, type CollectionDto } from '../../lib/api';

interface MetadataFormProps {
  onSubmit: (data: any) => void;
  isLoading: boolean;
}

export const MetadataForm: React.FC<MetadataFormProps> = ({ onSubmit, isLoading }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [visibility, setVisibility] = useState<'PRIVATE' | 'TEAM' | 'PUBLIC'>('PRIVATE');
  const [collectionId, setCollectionId] = useState('');
  
  const [collections, setCollections] = useState<CollectionDto[]>([]);
  const [tags, setTags] = useState<TagDto[]>([]);
  const [selectedTagIds, setSelectedTagIds] = useState<string[]>([]);
  
  useEffect(() => {
    // Fetch collections
    apiClient.getCollections(0, 100)
      .then(res => {
        if (res.content) setCollections(res.content);
      })
      .catch(console.error);

    // Fetch tags
    apiClient.getTags()
      .then(res => {
        if (Array.isArray(res)) setTags(res);
      })
      .catch(console.error);
  }, []);

  const handleTagToggle = (tagId: string) => {
    setSelectedTagIds(prev => 
      prev.includes(tagId) ? prev.filter(id => id !== tagId) : [...prev, tagId]
    );
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      title,
      description,
      visibility,
      collectionId: collectionId || undefined,
      tagIds: selectedTagIds
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label htmlFor="title" className="block text-sm font-medium text-gray-700">Title</label>
        <input
          type="text"
          id="title"
          required
          className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
      </div>

      <div>
        <label htmlFor="description" className="block text-sm font-medium text-gray-700">Description</label>
        <textarea
          id="description"
          rows={3}
          className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
      </div>

      <div>
        <label htmlFor="visibility" className="block text-sm font-medium text-gray-700">Visibility</label>
        <select
          id="visibility"
          className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md"
          value={visibility}
          onChange={(e) => setVisibility(e.target.value as 'PRIVATE' | 'TEAM' | 'PUBLIC')}
        >
          <option value="PRIVATE">Private</option>
          <option value="TEAM">Team</option>
          <option value="PUBLIC">Public</option>
        </select>
      </div>

      <div>
        <label htmlFor="collection" className="block text-sm font-medium text-gray-700">Collection</label>
        <select
          id="collection"
          className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md"
          value={collectionId}
          onChange={(e) => setCollectionId(e.target.value)}
        >
          <option value="">No Collection</option>
          {collections.map(c => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Tags</label>
        {tags.length === 0 ? (
          <p className="text-sm text-gray-500">No tags available. Create tags first.</p>
        ) : (
          <div className="flex flex-wrap gap-2">
            {tags.map(tag => {
              const isSelected = selectedTagIds.includes(tag.id!);
              return (
                <button
                  key={tag.id}
                  type="button"
                  onClick={() => handleTagToggle(tag.id!)}
                  className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium border transition-colors ${
                    isSelected 
                      ? 'border-transparent bg-indigo-100 text-indigo-800' 
                      : 'border-gray-300 bg-white text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <span
                    className="w-2 h-2 rounded-full"
                    style={{ backgroundColor: tag.color || '#6366F1' }}
                  />
                  {tag.name}
                </button>
              );
            })}
          </div>
        )}
      </div>

      <div className="pt-4 flex justify-end">
        <button
          type="submit"
          disabled={isLoading}
          className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
        >
          {isLoading ? 'Uploading...' : 'Upload Document'}
        </button>
      </div>
    </form>
  );
};

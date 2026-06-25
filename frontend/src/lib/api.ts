import { ChatApiError, getChatErrorMessage } from './chatErrors';

const API_BASE_URL = import.meta.env.PUBLIC_API_BASE_URL ?? 'http://localhost:8080/api';

export type Role = 'ADMIN' | 'EMPLOYEE' | 'VIEWER';

export interface User {
  id: string;
  fullName: string;
  email: string;
  role: Role;
  createdAt: string;
  updatedAt: string;
  disabled?: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  tokenType: 'Bearer';
  token: string;
  user: User;
}

export interface CollectionDto {
  id: string;
  name: string;
  description: string;
  ownerId: string;
  createdAt: string;
  updatedAt: string;
}

export interface TagDto {
  id: string;
  name: string;
  color: string;
}

export interface DocumentResponse {
  id: string;
  title: string;
  description: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  visibility: 'PRIVATE' | 'TEAM' | 'PUBLIC';
  ownerId: string;
  collection: CollectionDto | null;
  tags: TagDto[];
  createdAt: string;
  updatedAt: string;
}

export interface DocumentUploadRequest {
  title: string;
  description: string;
  visibility: 'PRIVATE' | 'TEAM' | 'PUBLIC';
  collectionId?: string;
  tagIds?: string[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
}

export type ChatConfidence = 'high' | 'medium' | 'low' | 'none';

export interface ChatSource {
  sourceIndex: number;
  documentId: string;
  documentTitle: string;
  chunkIndex: number;
  score: number;
  excerpt: string;
}

export interface ChatResponse {
  answer: string;
  sources: ChatSource[];
  confidence: ChatConfidence;
  retrievalCount: number;
}

export interface ChatRequest {
  question: string;
  topK?: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string = API_BASE_URL) {
    this.baseUrl = baseUrl;
  }

  private getToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('authToken');
    }
    return null;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (!response.ok) {
      if (response.status === 401 && typeof window !== 'undefined') {
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUser');
        window.dispatchEvent(new Event('auth-state-changed'));
      }
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
        errorData.message || `HTTP error! status: ${response.status}`
      );
    }

    return response.json();
  }

  async register(
    data: RegisterRequest
  ): Promise<ApiResponse<AuthResponse>> {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async login(data: LoginRequest): Promise<ApiResponse<AuthResponse>> {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async getUsers(page = 0, size = 20): Promise<ApiResponse<PageResponse<User>>> {
    return this.request(`/users?page=${page}&size=${size}`, {
      method: 'GET',
    });
  }

  async getUser(id: string): Promise<ApiResponse<User>> {
    return this.request(`/users/${id}`, {
      method: 'GET',
    });
  }

  async getCurrentUser(): Promise<ApiResponse<User>> {
    return this.request('/users/me', {
      method: 'GET',
    });
  }

  async updateUserRole(id: string, role: Role): Promise<ApiResponse<User>> {
    return this.request(`/users/${id}/role`, {
      method: 'PATCH',
      body: JSON.stringify({ role }),
    });
  }

  async deleteUser(id: string): Promise<ApiResponse<void>> {
    return this.request(`/users/${id}`, {
      method: 'DELETE',
    });
  }

  // --- Document APIs ---

  async getDocuments(page = 0, size = 20): Promise<PageResponse<DocumentResponse>> {
    return this.request(`/documents?page=${page}&size=${size}`, {
      method: 'GET',
    });
  }

  async searchDocuments(query: string, page = 0, size = 20): Promise<PageResponse<DocumentResponse>> {
    return this.request(`/documents/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`, {
      method: 'GET',
    });
  }

  async getDocument(id: string): Promise<DocumentResponse> {
    return this.request(`/documents/${id}`, {
      method: 'GET',
    });
  }

  async uploadDocument(file: File, metadata: DocumentUploadRequest): Promise<DocumentResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', metadata.title);
    if (metadata.description) formData.append('description', metadata.description);
    formData.append('visibility', metadata.visibility);
    if (metadata.collectionId) formData.append('collectionId', metadata.collectionId);
    if (metadata.tagIds && metadata.tagIds.length > 0) {
      metadata.tagIds.forEach(id => formData.append('tagIds', id));
    }

    const url = `${this.baseUrl}/documents`;
    const headers: Record<string, string> = {};
    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!response.ok) {
      if (response.status === 401 && typeof window !== 'undefined') {
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUser');
        window.dispatchEvent(new Event('auth-state-changed'));
      }
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  async updateDocument(id: string, metadata: DocumentUploadRequest): Promise<DocumentResponse> {
    return this.request(`/documents/${id}`, {
      method: 'PUT',
      body: JSON.stringify(metadata),
    });
  }

  async deleteDocument(id: string): Promise<void> {
    const url = `${this.baseUrl}/documents/${id}`;
    const headers: Record<string, string> = {};
    const token = this.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(url, {
      method: 'DELETE',
      headers,
    });
    if (!response.ok) {
      if (response.status === 401 && typeof window !== 'undefined') {
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUser');
        window.dispatchEvent(new Event('auth-state-changed'));
      }
      throw new Error('Failed to delete document');
    }
  }

  // --- Collection APIs ---

  async getCollections(page = 0, size = 20): Promise<PageResponse<CollectionDto>> {
    return this.request(`/collections?page=${page}&size=${size}`, {
      method: 'GET',
    });
  }

  async getCollection(id: string): Promise<CollectionDto> {
    return this.request(`/collections/${id}`, {
      method: 'GET',
    });
  }

  async createCollection(data: Partial<CollectionDto>): Promise<CollectionDto> {
    return this.request('/collections', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async updateCollection(id: string, data: Partial<CollectionDto>): Promise<CollectionDto> {
    return this.request(`/collections/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  async deleteCollection(id: string): Promise<void> {
    return this.request(`/collections/${id}`, {
      method: 'DELETE',
    });
  }

  // --- Tag APIs ---

  async getTags(): Promise<TagDto[]> {
    return this.request('/tags', {
      method: 'GET',
    });
  }

  async getTag(id: string): Promise<TagDto> {
    return this.request(`/tags/${id}`, {
      method: 'GET',
    });
  }

  async createTag(data: Partial<TagDto>): Promise<TagDto> {
    return this.request('/tags', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async updateTag(id: string, data: Partial<TagDto>): Promise<TagDto> {
    return this.request(`/tags/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  async deleteTag(id: string): Promise<void> {
    return this.request(`/tags/${id}`, {
      method: 'DELETE',
    });
  }

  async sendChatMessage(data: ChatRequest): Promise<ApiResponse<ChatResponse>> {
    const url = `${this.baseUrl}/chat`;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(data),
    });

    const payload = await response.json().catch(() => ({}));

    if (!response.ok) {
      if (response.status === 401 && typeof window !== 'undefined') {
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUser');
        window.dispatchEvent(new Event('auth-state-changed'));
      }
      throw new ChatApiError(
        response.status,
        getChatErrorMessage(response.status, payload.message)
      );
    }

    return payload;
  }
}

export const apiClient = new ApiClient();

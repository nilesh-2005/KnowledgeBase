import React, { useEffect, useState, useCallback } from 'react';
import { apiClient, type User, type Role } from '../../lib/api';
import { useAuth } from '../../lib/auth';

const ROLES: Role[] = ['ADMIN', 'EMPLOYEE', 'VIEWER'];

const roleColors: Record<Role, string> = {
  ADMIN: 'bg-red-100 text-red-700 ring-red-200',
  EMPLOYEE: 'bg-blue-100 text-blue-700 ring-blue-200',
  VIEWER: 'bg-gray-100 text-gray-600 ring-gray-200',
};

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
  });
}

export function AdminPanel() {
  const auth = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  // Guard: redirect non-admins
  useEffect(() => {
    if (!auth.isLoading && auth.user?.role !== 'ADMIN') {
      window.location.href = '/dashboard';
    }
  }, [auth.isLoading, auth.user]);

  const loadUsers = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await apiClient.getUsers(page, 20);
      if (res.success && res.data) {
        setUsers(res.data.content);
        setTotalPages(res.data.totalPages);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  const handleRoleChange = async (userId: string, newRole: Role) => {
    if (userId === auth.user?.id) {
      alert('You cannot change your own role.');
      return;
    }
    setUpdatingId(userId);
    try {
      const res = await apiClient.updateUserRole(userId, newRole);
      if (res.success && res.data) {
        setUsers(prev => prev.map(u => u.id === userId ? { ...u, role: res.data!.role } : u));
      }
    } catch (err: any) {
      alert(err.message || 'Failed to update role');
    } finally {
      setUpdatingId(null);
    }
  };

  const handleDelete = async (userId: string, userName: string) => {
    if (userId === auth.user?.id) {
      alert('You cannot delete your own account.');
      return;
    }
    if (!confirm(`Are you sure you want to permanently delete "${userName}"? This cannot be undone.`)) return;

    setDeletingId(userId);
    try {
      await apiClient.deleteUser(userId);
      setUsers(prev => prev.filter(u => u.id !== userId));
    } catch (err: any) {
      alert(err.message || 'Failed to delete user');
    } finally {
      setDeletingId(null);
    }
  };

  if (auth.isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-main">User Management</h1>
          <p className="mt-1 text-sm text-text-muted">
            Manage workspace members, roles, and access control.
          </p>
        </div>
        <div className="text-sm text-text-muted bg-panel border border-border rounded-lg px-4 py-2">
          <span className="font-semibold text-text-main">{users.length}</span> members shown
        </div>
      </div>

      {/* Error */}
      {error && (
        <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      {/* Table */}
      <div className="overflow-hidden rounded-xl border border-border bg-surface shadow-sm">
        <table className="min-w-full divide-y divide-border">
          <thead className="bg-panel">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wide text-text-muted">User</th>
              <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wide text-text-muted">Email</th>
              <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wide text-text-muted">Role</th>
              <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wide text-text-muted">Joined</th>
              <th className="px-6 py-3 text-right text-xs font-semibold uppercase tracking-wide text-text-muted">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {loading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <tr key={i}>
                  <td colSpan={5} className="px-6 py-4">
                    <div className="animate-pulse flex gap-4">
                      <div className="h-9 w-9 rounded-full bg-panel"></div>
                      <div className="flex-1 space-y-2 pt-1">
                        <div className="h-3 bg-panel rounded w-1/4"></div>
                        <div className="h-3 bg-panel rounded w-1/3"></div>
                      </div>
                    </div>
                  </td>
                </tr>
              ))
            ) : users.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-6 py-12 text-center text-text-muted text-sm">
                  No users found.
                </td>
              </tr>
            ) : (
              users.map(user => {
                const isCurrentUser = user.id === auth.user?.id;
                const isUpdating = updatingId === user.id;
                const isDeleting = deletingId === user.id;

                return (
                  <tr key={user.id} className={`transition-colors hover:bg-panel/50 ${isCurrentUser ? 'bg-indigo-50/30' : ''}`}>
                    {/* Avatar + Name */}
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 text-sm font-semibold text-white shadow-sm">
                          {user.fullName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)}
                        </div>
                        <div>
                          <div className="text-sm font-medium text-text-main">
                            {user.fullName}
                            {isCurrentUser && (
                              <span className="ml-2 text-xs font-normal text-indigo-500">(You)</span>
                            )}
                          </div>
                        </div>
                      </div>
                    </td>

                    {/* Email */}
                    <td className="px-6 py-4 text-sm text-text-muted">{user.email}</td>

                    {/* Role selector */}
                    <td className="px-6 py-4">
                      {isCurrentUser ? (
                        <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1 ring-inset ${roleColors[user.role]}`}>
                          {user.role}
                        </span>
                      ) : (
                        <div className="relative">
                          <select
                            value={user.role}
                            onChange={e => handleRoleChange(user.id, e.target.value as Role)}
                            disabled={isUpdating}
                            className={`appearance-none rounded-full px-3 py-0.5 text-xs font-semibold ring-1 ring-inset cursor-pointer focus:outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-wait ${roleColors[user.role]}`}
                          >
                            {ROLES.map(r => (
                              <option key={r} value={r}>{r}</option>
                            ))}
                          </select>
                          {isUpdating && (
                            <div className="absolute inset-0 flex items-center justify-center">
                              <div className="h-3 w-3 animate-spin rounded-full border border-current border-t-transparent"></div>
                            </div>
                          )}
                        </div>
                      )}
                    </td>

                    {/* Joined */}
                    <td className="px-6 py-4 text-sm text-text-muted">{formatDate(user.createdAt)}</td>

                    {/* Actions */}
                    <td className="px-6 py-4 text-right">
                      {!isCurrentUser && (
                        <button
                          onClick={() => handleDelete(user.id, user.fullName)}
                          disabled={isDeleting}
                          className="inline-flex items-center gap-1.5 rounded-md px-3 py-1.5 text-xs font-medium text-red-600 hover:bg-red-50 border border-transparent hover:border-red-200 transition-colors disabled:opacity-50 disabled:cursor-wait"
                        >
                          {isDeleting ? (
                            <>
                              <span className="h-3 w-3 animate-spin rounded-full border border-red-600 border-t-transparent"></span>
                              Deleting…
                            </>
                          ) : (
                            'Delete'
                          )}
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <button
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
            className="rounded-md border border-border bg-surface px-3 py-1.5 text-sm font-medium text-text-muted hover:bg-panel disabled:opacity-40 disabled:cursor-not-allowed"
          >
            Previous
          </button>
          <span className="text-sm text-text-muted">
            Page {page + 1} of {totalPages}
          </span>
          <button
            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="rounded-md border border-border bg-surface px-3 py-1.5 text-sm font-medium text-text-muted hover:bg-panel disabled:opacity-40 disabled:cursor-not-allowed"
          >
            Next
          </button>
        </div>
      )}

      {/* Role legend */}
      <div className="rounded-xl border border-border bg-panel px-5 py-4">
        <h3 className="text-xs font-semibold uppercase tracking-wide text-text-muted mb-3">Role Permissions</h3>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-3 text-sm">
          <div>
            <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1 ring-inset ${roleColors.VIEWER}`}>VIEWER</span>
            <ul className="mt-2 space-y-1 text-xs text-text-muted">
              <li>✅ View & search documents</li>
              <li>✅ Download documents</li>
              <li>✅ View collections & tags</li>
              <li>❌ Upload or delete documents</li>
              <li>❌ Manage tags or collections</li>
            </ul>
          </div>
          <div>
            <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1 ring-inset ${roleColors.EMPLOYEE}`}>EMPLOYEE</span>
            <ul className="mt-2 space-y-1 text-xs text-text-muted">
              <li>✅ Everything VIEWER can do</li>
              <li>✅ Upload documents</li>
              <li>✅ Delete own documents</li>
              <li>✅ Create & manage tags</li>
              <li>✅ Create & manage collections</li>
            </ul>
          </div>
          <div>
            <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1 ring-inset ${roleColors.ADMIN}`}>ADMIN</span>
            <ul className="mt-2 space-y-1 text-xs text-text-muted">
              <li>✅ Everything EMPLOYEE can do</li>
              <li>✅ Delete any document</li>
              <li>✅ Manage all users</li>
              <li>✅ Change user roles</li>
              <li>✅ Delete users</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}

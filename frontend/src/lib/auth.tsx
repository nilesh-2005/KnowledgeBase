import React, { useCallback, useEffect, useMemo, useState } from 'react';
import type { User } from './api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
  setUser: (user: User | null) => void;
}

interface StoredAuthState {
  user: User | null;
  token: string | null;
}

const TOKEN_KEY = 'authToken';
const USER_KEY = 'authUser';
const AUTH_CHANGED_EVENT = 'auth-state-changed';

function readStoredAuth(): StoredAuthState {
  if (typeof window === 'undefined') {
    return { user: null, token: null };
  }

  const token = localStorage.getItem(TOKEN_KEY);
  const storedUser = localStorage.getItem(USER_KEY);

  if (!token || !storedUser) {
    return { user: null, token: null };
  }

  try {
    return { token, user: JSON.parse(storedUser) as User };
  } catch (error) {
    console.error('Failed to restore auth state:', error);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    return { user: null, token: null };
  }
}

function notifyAuthChanged() {
  window.dispatchEvent(new Event(AUTH_CHANGED_EVENT));
}

function storeAuth(token: string, user: User) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
  notifyAuthChanged();
}

function clearStoredAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  notifyAuthChanged();
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}

export function useAuth(): AuthContextType {
  const [authState, setAuthState] = useState<StoredAuthState>({ user: null, token: null });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const syncAuthState = () => {
      setAuthState(readStoredAuth());
      setIsLoading(false);
    };

    syncAuthState();
    window.addEventListener(AUTH_CHANGED_EVENT, syncAuthState);
    window.addEventListener('storage', syncAuthState);

    return () => {
      window.removeEventListener(AUTH_CHANGED_EVENT, syncAuthState);
      window.removeEventListener('storage', syncAuthState);
    };
  }, []);

  const login = useCallback((newToken: string, newUser: User) => {
    setAuthState({ token: newToken, user: newUser });
    storeAuth(newToken, newUser);
  }, []);

  const logout = useCallback(() => {
    setAuthState({ token: null, user: null });
    clearStoredAuth();
  }, []);

  const setUser = useCallback((nextUser: User | null) => {
    setAuthState((current) => {
      if (!nextUser || !current.token) {
        clearStoredAuth();
        return { user: null, token: null };
      }

      storeAuth(current.token, nextUser);
      return { ...current, user: nextUser };
    });
  }, []);

  return useMemo(() => ({
    user: authState.user,
    token: authState.token,
    isAuthenticated: !!authState.token && !!authState.user,
    isLoading,
    login,
    logout,
    setUser,
  }), [authState, isLoading, login, logout, setUser]);
}

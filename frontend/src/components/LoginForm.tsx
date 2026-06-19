import React, { useState } from 'react';
import { apiClient, type LoginRequest } from '../lib/api';
import { useAuth } from '../lib/auth';

export function LoginForm() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const request: LoginRequest = { email, password };
      const response = await apiClient.login(request);

      if (response.status === 'success' && response.data) {
        login(response.data.token, response.data.user);
        setSuccess(true);
        // Redirect after 1 second
        setTimeout(() => {
          window.location.href = '/dashboard';
        }, 1000);
      } else {
        setError(response.message || 'Login failed');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setIsLoading(false);
    }
  };

  if (success) {
    return (
      <div className="text-center py-8">
        <p className="text-success text-body-strong mb-2">✓ Login successful!</p>
        <p className="text-mute">Redirecting to dashboard...</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4 w-full max-w-md">
      {error && (
        <div className="bg-red-100 border border-sale text-sale px-4 py-3 rounded-md text-body-md">
          {error}
        </div>
      )}
      
      <div>
        <label htmlFor="email" className="block text-body-strong text-ink mb-2">
          Email
        </label>
        <input
          id="email"
          type="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
          className="input-pill"
        />
      </div>

      <div>
        <label htmlFor="password" className="block text-body-strong text-ink mb-2">
          Password
        </label>
        <input
          id="password"
          type="password"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="••••••••"
          className="input-pill"
        />
      </div>

      <button
        type="submit"
        disabled={isLoading}
        className="btn-primary w-full disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {isLoading ? 'Signing In...' : 'Sign In'}
      </button>

      <div className="text-center">
        <p className="text-body-md text-mute">
          Don't have an account?{' '}
          <a href="/register" className="text-primary font-bold hover:text-charcoal">
            Sign Up
          </a>
        </p>
      </div>
    </form>
  );
}

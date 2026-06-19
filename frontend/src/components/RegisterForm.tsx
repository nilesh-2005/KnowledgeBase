import React, { useState } from 'react';
import { apiClient, type RegisterRequest } from '../lib/api';

export function RegisterForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    if (password.length < 6) {
      setError('Password must be at least 6 characters long');
      setIsLoading(false);
      return;
    }

    try {
      const request: RegisterRequest = { email, password, name };
      const response = await apiClient.register(request);

      if (response.status === 'success') {
        setSuccess(true);
        // Redirect to login after 2 seconds
        setTimeout(() => {
          window.location.href = '/login?registered=true';
        }, 2000);
      } else {
        setError(response.message || 'Registration failed');
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
        <p className="text-success text-body-strong mb-2">✓ Account created successfully!</p>
        <p className="text-mute">Redirecting to login...</p>
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
        <label htmlFor="name" className="block text-body-strong text-ink mb-2">
          Full Name
        </label>
        <input
          id="name"
          type="text"
          required
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="John Doe"
          className="input-pill"
        />
      </div>

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
        <p className="text-caption-sm text-mute mt-1">
          Minimum 6 characters
        </p>
      </div>

      <button
        type="submit"
        disabled={isLoading}
        className="btn-primary w-full disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {isLoading ? 'Creating Account...' : 'Create Account'}
      </button>

      <div className="text-center">
        <p className="text-body-md text-mute">
          Already have an account?{' '}
          <a href="/login" className="text-primary font-bold hover:text-charcoal">
            Sign In
          </a>
        </p>
      </div>
    </form>
  );
}

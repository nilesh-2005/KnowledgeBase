import React, { useState } from 'react';
import { apiClient, type RegisterRequest } from '../lib/api';
import { useAuth } from '../lib/auth';
import { Button } from './ui/Button';
import { Input } from './ui/Input';

export function RegisterForm() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    if (password.length < 8) {
      setError('Password must be at least 8 characters long');
      setIsLoading(false);
      return;
    }

    try {
      const request: RegisterRequest = { email, password, fullName };
      const response = await apiClient.register(request);

      if (response.success && response.data) {
        login(response.data.token, response.data.user);
        setSuccess(true);
        setTimeout(() => {
          window.location.href = '/dashboard';
        }, 1000);
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
      <div className="rounded-md border border-green-200 bg-green-50 px-4 py-3 text-sm">
        <p className="font-medium text-green-700">Account created successfully</p>
        <p className="mt-1 text-green-700/80">Redirecting to dashboard...</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="w-full space-y-4">
      {error && (
        <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-error">
          {error}
        </div>
      )}

      <div className="space-y-1.5">
        <label htmlFor="fullName" className="block text-sm font-medium text-text-main">
          Full Name
        </label>
        <Input
          id="fullName"
          type="text"
          required
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          placeholder="Jane Cooper"
        />
      </div>

      <div className="space-y-1.5">
        <label htmlFor="email" className="block text-sm font-medium text-text-main">
          Email
        </label>
        <Input
          id="email"
          type="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="name@company.com"
        />
      </div>

      <div className="space-y-1.5">
        <label htmlFor="password" className="block text-sm font-medium text-text-main">
          Password
        </label>
        <Input
          id="password"
          type="password"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
        />
        <p className="text-xs text-text-muted mt-1">
          Minimum 8 characters
        </p>
      </div>

      <Button
        type="submit"
        isLoading={isLoading}
        className="mt-2 w-full"
      >
        Create account
      </Button>

      <div className="mt-6 text-center">
        <p className="text-sm text-text-muted">
          Already have an account?{' '}
          <a href="/login" className="text-primary font-medium hover:text-text-muted transition-colors">
            Sign in
          </a>
        </p>
      </div>
    </form>
  );
}

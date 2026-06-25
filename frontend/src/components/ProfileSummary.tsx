import React from 'react';
import { Mail, Shield, UserCircle } from 'lucide-react';
import { useAuth } from '../lib/auth';
import { Badge } from './ui/Badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/Card';

export function ProfileSummary() {
  const { user } = useAuth();

  if (!user) {
    return null;
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Account</CardTitle>
        <CardDescription>Your authenticated workspace identity.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-md border border-border bg-surface">
            <UserCircle className="h-6 w-6 text-text-muted" />
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-text-main">{user.fullName}</p>
            <p className="truncate text-sm text-text-muted">{user.email}</p>
          </div>
        </div>

        <dl className="divide-y divide-border rounded-md border border-border">
          <div className="grid gap-1 px-3 py-2.5 sm:grid-cols-3 sm:gap-4">
            <dt className="flex items-center gap-2 text-sm text-text-muted">
              <Mail className="h-4 w-4" />
              Email
            </dt>
            <dd className="min-w-0 truncate text-sm font-medium text-text-main sm:col-span-2">{user.email}</dd>
          </div>
          <div className="grid gap-1 px-3 py-2.5 sm:grid-cols-3 sm:gap-4">
            <dt className="flex items-center gap-2 text-sm text-text-muted">
              <Shield className="h-4 w-4" />
              Role
            </dt>
            <dd className="sm:col-span-2">
              <Badge variant="secondary" className="capitalize">
                {user.role.toLowerCase()}
              </Badge>
            </dd>
          </div>
        </dl>
      </CardContent>
    </Card>
  );
}

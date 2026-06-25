# Knowledge Base Frontend

Astro frontend for the knowledge base platform.

## Phase 1

This phase includes the auth shell and dashboard entry points.

- Landing page
- Login page
- Register page
- Dashboard layout
- Navbar with auth-aware actions
- Auth state management with JWT storage
- Protected dashboard route
- Backend API client

## Environment Variables

- `PUBLIC_API_BASE_URL` - optional, defaults to `http://localhost:8080/api`

## Project Structure

```text
frontend/
├── astro.config.mjs
├── package.json
├── postcss.config.cjs
├── tailwind.config.cjs
├── src/
│   ├── components/
│   │   ├── AuthGuard.tsx
│   │   ├── LoginForm.tsx
│   │   ├── NavbarClient.tsx
│   │   ├── RegisterForm.tsx
│   │   └── UserMenu.tsx
│   ├── layouts/
│   │   └── Layout.astro
│   ├── lib/
│   │   ├── api.ts
│   │   └── auth.tsx
│   ├── pages/
│   │   ├── dashboard.astro
│   │   ├── index.astro
│   │   ├── login.astro
│   │   └── register.astro
│   └── styles/
│       └── globals.css
└── public/
```

## Commands

Run these from the `frontend/` directory:

```bash
npm install
npm run dev
npm run build
npm run preview
```

## Frontend Contract

The frontend expects the backend to expose JSON responses in this shape:

```json
{
	"success": true,
	"message": "Login successful",
	"data": {
		"tokenType": "Bearer",
		"token": "...",
		"user": {
			"id": "...",
			"fullName": "...",
			"email": "...",
			"role": "ADMIN"
		}
	}
}
```

## Notes

- JWT tokens are stored in `localStorage`.
- Protected routes redirect to `/login` when no token is present.
- The dashboard is client-protected and hydrates on the browser.
- Tailwind CSS v4 is used for the shared design tokens and utilities.

# Knowledge Base Platform

Full-stack knowledge base application with a Spring Boot backend and an Astro frontend.

## Current Phase 1

Phase 1 covers the auth-first product shell:

- JWT authentication and user registration
- Role-based user management
- PostgreSQL-backed persistence
- Astro login, register, dashboard, and protected route flow
- Client-side JWT storage and API access from the frontend

## Repository Layout

- `backend/` - Spring Boot API and database-backed auth system
- `frontend/` - Astro UI with React islands for auth state and forms
- `DESIGN.md` - visual and interaction direction for the frontend

## Required Environment Variables

Backend:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION` - optional, defaults to `PT2H`
- `JWT_ISSUER` - optional, defaults to `knowledge-base`
- `CORS_ALLOWED_ORIGINS`

Frontend:

- `PUBLIC_API_BASE_URL` - optional, defaults to `http://localhost:8080/api`

## Local Setup

1. Start the backend from `backend/`.
2. Start the frontend from `frontend/`.
3. Sign up, sign in, and open the dashboard to verify the auth flow end to end.

### Backend

```bash
cd backend
./mvnw.cmd spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Next Phases

- Document upload and management
- PDF processing
- Spring AI integration
- pgvector semantic retrieval
- RAG chat
- Semantic search
- Audit logs
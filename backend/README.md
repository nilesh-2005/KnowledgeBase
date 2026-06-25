# Knowledge Base Backend

Spring Boot 4 backend for the knowledge base platform.

## Phase 1

Phase 1 provides the auth and user-management foundation used by the Astro frontend.

- Java 21 and Spring Boot
- PostgreSQL persistence with Spring Data JPA
- JWT authentication for register and login
- Stateless Spring Security
- Role-based user access
- UUID identifiers and auditing timestamps
- Validation and global exception handling

## Package Structure

- `controller` - REST endpoints
- `service` - business logic and authorization checks
- `repository` - Spring Data JPA access
- `entity` - JPA entities and enums
- `dto` - request and response models
- `security` - JWT and principal handling
- `config` - properties, security, and CORS setup
- `exception` - API error mapping

## Required Environment Variables

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION` - optional, defaults to `PT2H`
- `JWT_ISSUER` - optional, defaults to `knowledge-base`
- `CORS_ALLOWED_ORIGINS`

## API Endpoints

### Authentication

- `POST /api/auth/register` - creates a user and returns a JWT response
- `POST /api/auth/login` - authenticates and returns a JWT response

### Users

- `GET /api/users/me` - current authenticated user
- `GET /api/users/current` - alias for the current authenticated user
- `GET /api/users` - paginated user list for admins
- `GET /api/users/{id}` - fetch a single user
- `PUT /api/users/{id}` - update a user
- `DELETE /api/users/{id}` - delete a user

## Response Format

The API uses a consistent envelope:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {}
}
```

Errors use the same envelope with `success: false` and `data: null`.

## Local Setup

1. Create a PostgreSQL database named `knowledgebase`.
2. Export the required environment variables.
3. Start the app:

```bash
./mvnw.cmd spring-boot:run
```

4. Run the tests:

```bash
./mvnw.cmd test
```

## Notes

- Controllers stay thin and delegate to services.
- Services hold business rules and authorization checks.
- DTOs isolate API contracts from persistence entities.
- JWT is used for stateless authentication.
- Spring Data auditing fills `createdAt` and `updatedAt` automatically.
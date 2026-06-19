# Knowledge Base Backend

Phase 1 delivers the production-ready backend foundation for the knowledge base platform.

## Included in Phase 1

- Java 21 Spring Boot backend
- PostgreSQL persistence with Spring Data JPA
- JWT authentication with register and login APIs
- Spring Security with stateless sessions
- Role-based user management
- UUID primary keys and auditing timestamps
- Validation and global exception handling

## Excluded until later phases

- Spring AI
- pgvector
- Embeddings and RAG
- Semantic search
- Document chunking and upload processing
- OpenAI and Ollama integration
- Docker deployment
- Microservices

## Package structure

- `controller` for REST endpoints
- `service` for business rules and transaction boundaries
- `repository` for Spring Data JPA access
- `entity` for JPA entities and enums
- `dto` for request and response objects
- `security` for JWT and user principal handling
- `config` for security, CORS, and properties
- `exception` for consistent API errors

## API endpoints

### Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`

### Users

- `GET /api/users/me`
- `GET /api/users`
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

## Response format

Success:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

Failure:

```json
{
  "success": false,
  "message": "Error description"
}
```

## Local setup

1. Create a PostgreSQL database named `knowledgebase`.
2. Set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `JWT_SECRET`.
3. Run the backend:

```bash
./mvnw.cmd spring-boot:run
```

4. Run tests:

```bash
./mvnw.cmd test
```

## Design notes

- Controllers stay thin and delegate to services.
- Services hold business rules and authorization checks.
- DTOs isolate API contracts from persistence entities.
- JWT is used for stateless authentication.
- Spring Data auditing fills `createdAt` and `updatedAt` automatically.
- The structure leaves room for future document and AI modules without changing the auth core.
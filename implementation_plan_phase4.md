# Implementation Plan: Phase 4 (Embedding Generation & Vector Storage)

## Goal
Generate embeddings for existing document chunks, persist them in pgvector, and build a foundational backend similarity-search service, without touching the UI or ChatGPT integration yet.

## Proposed Changes

### 1. Database Indexing & Migrations
- **[MODIFY] [V2__add_pgvector.sql](file:///C:/Users/frien/OneDrive/Desktop/Knowledge-base/backend/src/main/resources/db/migration/V2__add_pgvector.sql)**: Update `ADD COLUMN embedding` to `ADD COLUMN IF NOT EXISTS embedding` to prevent Flyway from crashing on startup (since our restored database already has this column).
- **[NEW] `V3__add_vector_index.sql`**: Create an index on the `embedding` column for optimized similarity search.
  ```sql
  CREATE INDEX idx_document_chunks_embedding ON document_chunks USING hnsw (embedding vector_cosine_ops);
  ```

### 2. Embedding Generation (EmbeddingService)
- **[MODIFY] [pom.xml](file:///C:/Users/frien/OneDrive/Desktop/Knowledge-base/backend/pom.xml)**: Add `spring-ai-ollama-spring-boot-starter` and the Spring AI BOM to standardize integration.
- **[MODIFY] [application.properties](file:///C:/Users/frien/OneDrive/Desktop/Knowledge-base/backend/src/main/resources/application.properties)**: Configure the Spring AI Ollama embedding model (defaulting to `nomic-embed-text`).
- **[DELETE] `DummyAiServiceImpl.java`**: Remove the stubbed out implementation.
- **[NEW] `OllamaEmbeddingServiceImpl.java`**: Implement the `EmbeddingService` interface by injecting Spring AI's `EmbeddingModel`.

### 3. Vector Persistence (VectorStoreService)
- **[NEW] `PgVectorStoreServiceImpl.java`**: Implement the `VectorStoreService` interface. It will receive chunks, invoke the `EmbeddingService` to compute vectors, set the `embedding` fields, and `saveAll` via `DocumentChunkRepository`.

### 4. Similarity Search (SimilaritySearchService)
- **[MODIFY] [DocumentChunkRepository.java](file:///C:/Users/frien/OneDrive/Desktop/Knowledge-base/backend/src/main/java/com/nilesh/knowledgebase/repository/DocumentChunkRepository.java)**: Add an HQL query leveraging Hibernate Vector's native functions.
  ```java
  @Query("SELECT c FROM DocumentChunk c ORDER BY l2_distance(c.embedding, :embedding)")
  List<DocumentChunk> findSimilarChunks(@Param("embedding") float[] embedding, Pageable pageable);
  ```
- **[NEW] `PgSimilaritySearchServiceImpl.java`**: Implement `SimilaritySearchService` to embed a user query and execute the top-K search.

### 5. Backfilling Existing Chunks
- **[NEW] `VectorBackfillRunner.java`**: Create a temporary `CommandLineRunner` component that runs on startup, finds any chunks with a `null` embedding (we have 7 of them from your test data), and automatically generates and persists their embeddings.

## Verification Plan
- Compile and build successfully.
- Observe the `CommandLineRunner` logs to confirm it generated 7 embeddings for the existing database chunks.
- Connect to the `knowledgebase-db` Docker container and query the database directly to confirm the `embedding` column is populated with actual vectors instead of `null`.

> [!IMPORTANT]
> **Plan Adjusted to Match Master Summary**
> The implementation plan was adjusted to use **Ollama** and the `nomic-embed-text` model, instead of OpenAI, to comply with the project's Master Summary.

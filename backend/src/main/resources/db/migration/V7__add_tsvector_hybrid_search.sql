ALTER TABLE document_chunks ADD COLUMN search_vector tsvector;
CREATE INDEX idx_document_chunks_search_vector ON document_chunks USING GIN(search_vector);

CREATE OR REPLACE FUNCTION document_chunks_search_trigger() RETURNS trigger AS $$
BEGIN
  NEW.search_vector := to_tsvector('english', coalesce(NEW.content, ''));
  RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate BEFORE INSERT OR UPDATE
  ON document_chunks FOR EACH ROW EXECUTE FUNCTION document_chunks_search_trigger();

-- Backfill existing chunks
UPDATE document_chunks SET content = content;

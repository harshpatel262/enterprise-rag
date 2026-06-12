-- Schema for the "postgres" profile. Requires the pgvector extension
-- (the docker-compose image ships with it).

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS chunks (
    id              UUID PRIMARY KEY,
    document_id     UUID         NOT NULL,
    document_title  TEXT         NOT NULL,
    sequence        INT          NOT NULL,
    text            TEXT         NOT NULL,
    embedding       vector(256)  NOT NULL
);

CREATE INDEX IF NOT EXISTS chunks_embedding_idx
    ON chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

CREATE INDEX IF NOT EXISTS chunks_document_idx ON chunks (document_id);

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE product_embeddings (
    product_id    BIGINT        PRIMARY KEY REFERENCES products (id) ON DELETE CASCADE,
    embedding     vector(1536)  NOT NULL,
    content_hash  VARCHAR(64)   NOT NULL,
    model         VARCHAR(100)  NOT NULL,
    embedded_at   TIMESTAMPTZ   NOT NULL
);
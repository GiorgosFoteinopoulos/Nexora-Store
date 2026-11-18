
CREATE INDEX ix_product_embeddings_hnsw ON product_embeddings USING hnsw (embedding vector_cosine_ops);
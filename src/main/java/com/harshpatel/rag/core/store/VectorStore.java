package com.harshpatel.rag.core.store;

import com.harshpatel.rag.core.model.DocumentChunk;
import java.util.List;

/**
 * Vector persistence and similarity search. Two implementations ship:
 * an in-memory store for local development and tests, and a pgvector
 * store (profile "postgres") for real deployments.
 */
public interface VectorStore {

    void upsert(List<DocumentChunk> chunks);

    List<ScoredChunk> search(float[] queryEmbedding, int topK);

    long count();
}

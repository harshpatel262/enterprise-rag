package com.harshpatel.rag.core.store;

import com.harshpatel.rag.core.model.DocumentChunk;

/** A retrieved chunk with its cosine similarity to the query. */
public record ScoredChunk(DocumentChunk chunk, double score) {
}

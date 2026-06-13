package com.harshpatel.rag.core.retrieval;

import com.harshpatel.rag.core.embedding.EmbeddingClient;
import com.harshpatel.rag.core.store.ScoredChunk;
import com.harshpatel.rag.core.store.VectorStore;
import java.util.List;

/** Dense retrieval: embed the query, then cosine-search the vector store. */
public final class VectorRetriever implements Retriever {

    private final EmbeddingClient embedder;
    private final VectorStore store;

    public VectorRetriever(EmbeddingClient embedder, VectorStore store) {
        this.embedder = embedder;
        this.store = store;
    }

    @Override
    public List<ScoredChunk> retrieve(String query, int topK) {
        return store.search(embedder.embed(query), topK);
    }
}

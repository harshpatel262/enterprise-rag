package com.harshpatel.rag.service;

import com.harshpatel.rag.core.chunking.ChunkingStrategy;
import com.harshpatel.rag.core.embedding.EmbeddingClient;
import com.harshpatel.rag.core.model.DocumentChunk;
import com.harshpatel.rag.core.store.VectorStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class IngestionService {

    private final ChunkingStrategy chunker;
    private final EmbeddingClient embedder;
    private final VectorStore store;

    public IngestionService(ChunkingStrategy chunker, EmbeddingClient embedder,
                            VectorStore store) {
        this.chunker = chunker;
        this.embedder = embedder;
        this.store = store;
    }

    /** Chunk, embed, and index a document. Returns (documentId, chunkCount). */
    public IngestResult ingest(String title, String content) {
        UUID documentId = UUID.randomUUID();
        List<String> pieces = chunker.chunk(content);

        List<DocumentChunk> chunks = new ArrayList<>(pieces.size());
        for (int i = 0; i < pieces.size(); i++) {
            chunks.add(new DocumentChunk(
                    UUID.randomUUID(), documentId, title, i,
                    pieces.get(i), embedder.embed(pieces.get(i))));
        }
        store.upsert(chunks);
        return new IngestResult(documentId, chunks.size());
    }

    public long indexedChunks() {
        return store.count();
    }

    public record IngestResult(UUID documentId, int chunkCount) {
    }
}

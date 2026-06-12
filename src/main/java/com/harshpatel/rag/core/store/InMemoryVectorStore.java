package com.harshpatel.rag.core.store;

import com.harshpatel.rag.core.model.DocumentChunk;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Brute-force cosine scan. Fine into the tens of thousands of chunks. */
@Component
@Profile("!postgres")
public class InMemoryVectorStore implements VectorStore {

    private final Map<UUID, DocumentChunk> chunks = new ConcurrentHashMap<>();

    @Override
    public void upsert(List<DocumentChunk> incoming) {
        for (DocumentChunk chunk : incoming) {
            chunks.put(chunk.id(), chunk);
        }
    }

    @Override
    public List<ScoredChunk> search(float[] queryEmbedding, int topK) {
        return chunks.values().stream()
                .map(chunk -> new ScoredChunk(chunk, cosine(queryEmbedding, chunk.embedding())))
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(topK)
                .toList();
    }

    @Override
    public long count() {
        return chunks.size();
    }

    private static double cosine(float[] a, float[] b) {
        double dot = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
        }
        return dot; // vectors are L2-normalized at embed time
    }
}

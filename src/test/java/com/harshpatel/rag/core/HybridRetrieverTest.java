package com.harshpatel.rag.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.harshpatel.rag.core.lexical.InMemoryLexicalIndex;
import com.harshpatel.rag.core.model.DocumentChunk;
import com.harshpatel.rag.core.retrieval.HybridRetriever;
import com.harshpatel.rag.core.retrieval.Retriever;
import com.harshpatel.rag.core.store.ScoredChunk;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HybridRetrieverTest {

    private static DocumentChunk chunk(String title, String text) {
        return new DocumentChunk(UUID.randomUUID(), UUID.randomUUID(), title, 0, text, new float[0]);
    }

    @Test
    void fusesDenseAndSparseRankings() {
        var a = chunk("A", "vector embeddings similarity search");          // dense-only hit
        var b = chunk("B", "kafka streams process kafka events");          // both lists
        var c = chunk("C", "kafka is an append only log");                 // sparse-only hit

        // Stub dense retriever ranks A then B; it never surfaces C.
        Retriever dense = (query, topK) -> List.of(
                new ScoredChunk(a, 0.9), new ScoredChunk(b, 0.8));

        var sparse = new InMemoryLexicalIndex();
        sparse.index(List.of(a, b, c));

        var hybrid = new HybridRetriever(dense, sparse, 60);
        List<ScoredChunk> results = hybrid.retrieve("kafka", 3);

        List<String> titles = results.stream().map(r -> r.chunk().documentTitle()).toList();
        // B is the only chunk in BOTH lists, so RRF ranks it first.
        assertThat(titles.get(0)).isEqualTo("B");
        // A (dense-only) and C (sparse-only) are both recovered by fusion.
        assertThat(titles).containsExactlyInAnyOrder("A", "B", "C");
    }

    @Test
    void surfacesExactMatchThatDenseMisses() {
        var dense1 = chunk("Dense1", "semantic neighbors of the query topic");
        var dense2 = chunk("Dense2", "loosely related background material");
        var exact = chunk("Exact", "the term idempotency appears here verbatim");

        // Dense retriever is blind to the exact-keyword chunk.
        Retriever dense = (query, topK) -> List.of(
                new ScoredChunk(dense1, 0.7), new ScoredChunk(dense2, 0.6));

        var sparse = new InMemoryLexicalIndex();
        sparse.index(List.of(dense1, dense2, exact));

        var hybrid = new HybridRetriever(dense, sparse, 60);
        List<String> titles = hybrid.retrieve("idempotency", 3).stream()
                .map(r -> r.chunk().documentTitle()).toList();

        assertThat(titles).contains("Exact"); // BM25 rescues a hit dense search missed
    }
}

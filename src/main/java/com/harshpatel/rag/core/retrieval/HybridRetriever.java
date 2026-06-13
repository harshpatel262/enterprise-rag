package com.harshpatel.rag.core.retrieval;

import com.harshpatel.rag.core.lexical.LexicalIndex;
import com.harshpatel.rag.core.model.DocumentChunk;
import com.harshpatel.rag.core.store.ScoredChunk;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fuses dense (vector) and sparse (BM25) retrieval with
 * <a href="https://plg.uwaterloo.ca/~gvcormack/cormacksigir09-rrf.pdf">Reciprocal
 * Rank Fusion</a>. RRF combines ranked lists by rank rather than raw score,
 * so it needs no score calibration between the two very different scales
 * (cosine similarity vs. BM25) — each chunk's fused score is
 * {@code sum over lists of 1 / (k + rank)}.
 *
 * <p>This captures both axes of relevance: vector search handles paraphrase
 * and semantics, BM25 guarantees exact-term and rare-token matches that
 * embeddings blur. Each retriever contributes a candidate pool larger than
 * the requested top-k so a chunk ranked highly by only one method can still
 * win after fusion.
 */
public final class HybridRetriever implements Retriever {

    private final Retriever dense;
    private final LexicalIndex sparse;
    private final int rrfK;

    public HybridRetriever(Retriever dense, LexicalIndex sparse, int rrfK) {
        this.dense = dense;
        this.sparse = sparse;
        this.rrfK = rrfK;
    }

    @Override
    public List<ScoredChunk> retrieve(String query, int topK) {
        int candidatePool = Math.max(topK, 10);
        List<ScoredChunk> denseHits = dense.retrieve(query, candidatePool);
        List<ScoredChunk> sparseHits = sparse.search(query, candidatePool);

        Map<UUID, DocumentChunk> chunksById = new LinkedHashMap<>();
        Map<UUID, Double> fused = new LinkedHashMap<>();
        accumulate(denseHits, chunksById, fused);
        accumulate(sparseHits, chunksById, fused);

        List<ScoredChunk> ranked = new ArrayList<>(fused.size());
        for (Map.Entry<UUID, Double> entry : fused.entrySet()) {
            ranked.add(new ScoredChunk(chunksById.get(entry.getKey()), entry.getValue()));
        }
        ranked.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());
        return ranked.subList(0, Math.min(topK, ranked.size()));
    }

    private void accumulate(
            List<ScoredChunk> hits, Map<UUID, DocumentChunk> chunks, Map<UUID, Double> fused) {
        for (int rank = 0; rank < hits.size(); rank++) {
            DocumentChunk chunk = hits.get(rank).chunk();
            chunks.putIfAbsent(chunk.id(), chunk);
            fused.merge(chunk.id(), 1.0 / (rrfK + rank + 1), Double::sum);
        }
    }
}

package com.harshpatel.rag.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.harshpatel.rag.core.embedding.HashingEmbeddingClient;
import com.harshpatel.rag.core.model.DocumentChunk;
import com.harshpatel.rag.core.store.InMemoryVectorStore;
import com.harshpatel.rag.core.store.ScoredChunk;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryVectorStoreTest {

    private final HashingEmbeddingClient embedder = new HashingEmbeddingClient();

    private DocumentChunk chunk(String title, String text) {
        return new DocumentChunk(
                UUID.randomUUID(), UUID.randomUUID(), title, 0, text, embedder.embed(text));
    }

    @Test
    void returnsMostSimilarChunkFirst() {
        var store = new InMemoryVectorStore();
        store.upsert(List.of(
                chunk("Refunds", "Customers may request a refund within 30 days of purchase."),
                chunk("Travel", "Employees book travel through the corporate travel portal."),
                chunk("Security", "Production access requires hardware security keys.")));

        List<ScoredChunk> results =
                store.search(embedder.embed("how do I request a refund for a purchase"), 2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).chunk().documentTitle()).isEqualTo("Refunds");
        assertThat(results.get(0).score()).isGreaterThan(results.get(1).score());
    }

    @Test
    void upsertReplacesExistingChunk() {
        var store = new InMemoryVectorStore();
        DocumentChunk original = chunk("Doc", "original text");
        store.upsert(List.of(original));
        store.upsert(List.of(new DocumentChunk(
                original.id(), original.documentId(), "Doc", 0,
                "updated text", embedder.embed("updated text"))));

        assertThat(store.count()).isEqualTo(1);
    }
}

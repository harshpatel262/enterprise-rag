package com.harshpatel.rag.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.harshpatel.rag.core.lexical.InMemoryLexicalIndex;
import com.harshpatel.rag.core.model.DocumentChunk;
import com.harshpatel.rag.core.store.ScoredChunk;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class Bm25LexicalIndexTest {

    private static DocumentChunk chunk(String title, String text) {
        return new DocumentChunk(UUID.randomUUID(), UUID.randomUUID(), title, 0, text, new float[0]);
    }

    @Test
    void ranksExactKeywordMatchFirst() {
        var index = new InMemoryLexicalIndex();
        index.index(List.of(
                chunk("Kafka", "Kafka streams process Kafka events with partitions and offsets."),
                chunk("Vectors", "Embeddings capture semantic similarity between documents."),
                chunk("Logs", "A commit log stores an append-only sequence of records.")));

        List<ScoredChunk> results = index.search("kafka partitions", 3);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).chunk().documentTitle()).isEqualTo("Kafka");
    }

    @Test
    void higherTermFrequencyOutranksLower() {
        var index = new InMemoryLexicalIndex();
        var twice = chunk("Twice", "kafka streams process kafka events");
        var once = chunk("Once", "kafka is an append only log");
        index.index(List.of(twice, once, chunk("None", "vectors and embeddings only")));

        List<ScoredChunk> results = index.search("kafka", 3);

        assertThat(results).hasSize(2); // the "None" chunk scores zero and is excluded
        assertThat(results.get(0).chunk().documentTitle()).isEqualTo("Twice");
        assertThat(results.get(1).chunk().documentTitle()).isEqualTo("Once");
    }

    @Test
    void emptyIndexReturnsNoResults() {
        assertThat(new InMemoryLexicalIndex().search("anything", 5)).isEmpty();
    }

    @Test
    void termAbsentFromCorpusReturnsNoResults() {
        var index = new InMemoryLexicalIndex();
        index.index(List.of(chunk("Doc", "kafka streams and partitions")));
        assertThat(index.search("kubernetes", 5)).isEmpty();
    }
}

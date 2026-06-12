package com.harshpatel.rag.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.harshpatel.rag.core.chunking.ParagraphChunker;
import com.harshpatel.rag.core.chunking.SlidingWindowChunker;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChunkingStrategyTest {

    @Test
    void paragraphChunkerKeepsShortDocumentWhole() {
        var chunker = new ParagraphChunker(1200);
        List<String> chunks = chunker.chunk("First paragraph.\n\nSecond paragraph.");
        assertThat(chunks).containsExactly("First paragraph.\n\nSecond paragraph.");
    }

    @Test
    void paragraphChunkerSplitsAtBudget() {
        var chunker = new ParagraphChunker(100);
        String paragraph = "x".repeat(80);
        List<String> chunks = chunker.chunk(paragraph + "\n\n" + paragraph + "\n\n" + paragraph);
        assertThat(chunks).hasSize(3);
        assertThat(chunks).allSatisfy(chunk -> assertThat(chunk.length()).isLessThanOrEqualTo(100));
    }

    @Test
    void paragraphChunkerHardSplitsOversizedParagraph() {
        var chunker = new ParagraphChunker(100);
        List<String> chunks = chunker.chunk("y".repeat(250));
        assertThat(chunks).hasSize(3);
        assertThat(String.join("", chunks)).hasSize(250);
    }

    @Test
    void slidingWindowOverlapsConsecutiveChunks() {
        var chunker = new SlidingWindowChunker(100, 20);
        String text = "a".repeat(70) + "b".repeat(70) + "c".repeat(70);
        List<String> chunks = chunker.chunk(text);

        assertThat(chunks.size()).isGreaterThan(1);
        for (int i = 1; i < chunks.size(); i++) {
            String previousTail = chunks.get(i - 1).substring(chunks.get(i - 1).length() - 20);
            assertThat(chunks.get(i)).startsWith(previousTail);
        }
    }

    @Test
    void slidingWindowRejectsInvalidOverlap() {
        assertThatThrownBy(() -> new SlidingWindowChunker(100, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

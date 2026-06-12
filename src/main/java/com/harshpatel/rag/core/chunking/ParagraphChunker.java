package com.harshpatel.rag.core.chunking;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits on paragraph boundaries, then greedily packs paragraphs into
 * chunks up to {@code maxChars}. Keeps semantic units intact, which
 * usually retrieves better than fixed windows on prose documents.
 */
public final class ParagraphChunker implements ChunkingStrategy {

    private final int maxChars;

    public ParagraphChunker(int maxChars) {
        if (maxChars < 100) {
            throw new IllegalArgumentException("maxChars must be >= 100");
        }
        this.maxChars = maxChars;
    }

    @Override
    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String paragraph : text.split("\\n\\s*\\n")) {
            String trimmed = paragraph.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (!current.isEmpty() && current.length() + trimmed.length() + 2 > maxChars) {
                chunks.add(current.toString());
                current.setLength(0);
            }
            if (!current.isEmpty()) {
                current.append("\n\n");
            }
            // a single paragraph larger than the budget is split hard
            while (trimmed.length() > maxChars) {
                chunks.add(trimmed.substring(0, maxChars));
                trimmed = trimmed.substring(maxChars);
            }
            current.append(trimmed);
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }
}

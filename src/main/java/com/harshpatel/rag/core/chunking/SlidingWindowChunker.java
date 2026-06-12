package com.harshpatel.rag.core.chunking;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixed-size character windows with overlap. Robust default for content
 * without reliable paragraph structure (logs, transcripts, exports).
 */
public final class SlidingWindowChunker implements ChunkingStrategy {

    private final int windowChars;
    private final int overlapChars;

    public SlidingWindowChunker(int windowChars, int overlapChars) {
        if (windowChars < 100) {
            throw new IllegalArgumentException("windowChars must be >= 100");
        }
        if (overlapChars < 0 || overlapChars >= windowChars) {
            throw new IllegalArgumentException("overlap must be in [0, windowChars)");
        }
        this.windowChars = windowChars;
        this.overlapChars = overlapChars;
    }

    @Override
    public List<String> chunk(String text) {
        String normalized = text.strip();
        List<String> chunks = new ArrayList<>();
        int step = windowChars - overlapChars;
        for (int start = 0; start < normalized.length(); start += step) {
            int end = Math.min(start + windowChars, normalized.length());
            chunks.add(normalized.substring(start, end));
            if (end == normalized.length()) {
                break;
            }
        }
        return chunks;
    }
}

package com.harshpatel.rag.core.model;

import java.util.UUID;

/** One retrieval unit: a chunk of a source document plus its embedding. */
public record DocumentChunk(
        UUID id,
        UUID documentId,
        String documentTitle,
        int sequence,
        String text,
        float[] embedding
) {
}

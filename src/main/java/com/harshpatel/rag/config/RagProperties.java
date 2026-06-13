package com.harshpatel.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * All tuning knobs in one place. Chunking, retrieval depth, and the
 * generation model are configuration, not code — the same build serves
 * different corpora with different settings.
 */
@ConfigurationProperties(prefix = "rag")
public record RagProperties(
        String chunker,          // "paragraph" | "sliding-window"
        int chunkSize,           // max characters per chunk
        int chunkOverlap,        // overlap characters (sliding-window only)
        int defaultTopK,
        String retriever,        // "hybrid" | "vector"
        int rrfK,                // reciprocal rank fusion constant (hybrid only)
        String anthropicApiKey,  // blank -> offline extractive answerer
        String model
) {
    public RagProperties {
        chunker = (chunker == null || chunker.isBlank()) ? "paragraph" : chunker;
        chunkSize = chunkSize <= 0 ? 1200 : chunkSize;
        chunkOverlap = chunkOverlap < 0 ? 200 : chunkOverlap;
        defaultTopK = defaultTopK <= 0 ? 5 : defaultTopK;
        retriever = (retriever == null || retriever.isBlank()) ? "hybrid" : retriever;
        rrfK = rrfK <= 0 ? 60 : rrfK;
        model = (model == null || model.isBlank()) ? "claude-sonnet-4-6" : model;
    }
}

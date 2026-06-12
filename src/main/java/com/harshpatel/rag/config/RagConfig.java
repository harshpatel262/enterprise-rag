package com.harshpatel.rag.config;

import com.harshpatel.rag.core.chunking.ChunkingStrategy;
import com.harshpatel.rag.core.chunking.ParagraphChunker;
import com.harshpatel.rag.core.chunking.SlidingWindowChunker;
import com.harshpatel.rag.core.embedding.EmbeddingClient;
import com.harshpatel.rag.core.embedding.HashingEmbeddingClient;
import com.harshpatel.rag.core.llm.AnthropicLlmClient;
import com.harshpatel.rag.core.llm.ExtractiveLlmClient;
import com.harshpatel.rag.core.llm.LlmClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires concrete strategies from configuration. Each collaborator is an
 * interface (strategy pattern), so chunkers, embedders, and LLM backends
 * are swappable without touching the ingestion or query pipelines.
 */
@Configuration
public class RagConfig {

    @Bean
    ChunkingStrategy chunkingStrategy(RagProperties properties) {
        return switch (properties.chunker()) {
            case "sliding-window" ->
                    new SlidingWindowChunker(properties.chunkSize(), properties.chunkOverlap());
            case "paragraph" -> new ParagraphChunker(properties.chunkSize());
            default -> throw new IllegalArgumentException(
                    "unknown chunker: " + properties.chunker());
        };
    }

    @Bean
    EmbeddingClient embeddingClient() {
        return new HashingEmbeddingClient();
    }

    @Bean
    LlmClient llmClient(RagProperties properties) {
        if (properties.anthropicApiKey() == null || properties.anthropicApiKey().isBlank()) {
            return new ExtractiveLlmClient();
        }
        return new AnthropicLlmClient(properties.anthropicApiKey(), properties.model());
    }
}

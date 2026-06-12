package com.harshpatel.rag.core.llm;

import com.harshpatel.rag.core.store.ScoredChunk;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestClient;

/**
 * Claude-backed answerer. The prompt enforces grounding: answer only from
 * the numbered context passages and cite them as [n]; say so when the
 * context is insufficient instead of guessing.
 */
public final class AnthropicLlmClient implements LlmClient {

    private static final String SYSTEM = """
            You are an enterprise knowledge assistant. Answer the user's question \
            using ONLY the numbered context passages provided. Cite passages inline \
            as [1], [2], etc. If the context does not contain the answer, say exactly \
            that — do not use outside knowledge or guess.""";

    private final RestClient http;
    private final String model;

    public AnthropicLlmClient(String apiKey, String model) {
        this.model = model;
        this.http = RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String answer(String question, List<ScoredChunk> context) {
        StringBuilder prompt = new StringBuilder("Context passages:\n\n");
        for (int i = 0; i < context.size(); i++) {
            prompt.append('[').append(i + 1).append("] (")
                    .append(context.get(i).chunk().documentTitle()).append(")\n")
                    .append(context.get(i).chunk().text()).append("\n\n");
        }
        prompt.append("Question: ").append(question);

        Map<String, Object> body = http.post()
                .uri("/v1/messages")
                .body(Map.of(
                        "model", model,
                        "max_tokens", 1024,
                        "system", SYSTEM,
                        "messages", List.of(
                                Map.of("role", "user", "content", prompt.toString()))))
                .retrieve()
                .body(Map.class);

        if (body == null || !(body.get("content") instanceof List<?> blocks)) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (Object block : blocks) {
            if (block instanceof Map<?, ?> map && "text".equals(map.get("type"))) {
                text.append(map.get("text"));
            }
        }
        return text.toString();
    }
}

package com.harshpatel.rag.core.llm;

import com.harshpatel.rag.core.store.ScoredChunk;
import java.util.List;

/**
 * Offline fallback used when no LLM API key is configured: returns the
 * top retrieved passages verbatim with citations. Retrieval quality is
 * fully exercisable (and testable) without any external dependency.
 */
public final class ExtractiveLlmClient implements LlmClient {

    @Override
    public String answer(String question, List<ScoredChunk> context) {
        if (context.isEmpty()) {
            return "No relevant passages found in the knowledge base.";
        }
        StringBuilder answer = new StringBuilder(
                "Most relevant passages for: \"" + question + "\"\n\n");
        for (int i = 0; i < context.size(); i++) {
            answer.append('[').append(i + 1).append("] ")
                    .append(context.get(i).chunk().text().strip())
                    .append("\n\n");
        }
        return answer.toString().strip();
    }
}

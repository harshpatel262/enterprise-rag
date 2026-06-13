package com.harshpatel.rag.service;

import com.harshpatel.rag.api.dto.Citation;
import com.harshpatel.rag.api.dto.QueryResponse;
import com.harshpatel.rag.config.RagProperties;
import com.harshpatel.rag.core.llm.LlmClient;
import com.harshpatel.rag.core.retrieval.Retriever;
import com.harshpatel.rag.core.store.ScoredChunk;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QueryService {

    private final Retriever retriever;
    private final LlmClient llm;
    private final RagProperties properties;

    public QueryService(Retriever retriever, LlmClient llm, RagProperties properties) {
        this.retriever = retriever;
        this.llm = llm;
        this.properties = properties;
    }

    public QueryResponse query(String question, Integer topK) {
        int k = (topK == null || topK <= 0) ? properties.defaultTopK() : topK;

        long start = System.nanoTime();
        List<ScoredChunk> retrieved = retriever.retrieve(question, k);
        long retrievalMs = (System.nanoTime() - start) / 1_000_000;

        String answer = llm.answer(question, retrieved);
        List<Citation> citations = retrieved.stream()
                .map(scored -> new Citation(
                        scored.chunk().documentTitle(),
                        scored.chunk().sequence(),
                        snippet(scored.chunk().text()),
                        Math.round(scored.score() * 1000.0) / 1000.0))
                .toList();
        return new QueryResponse(answer, citations, retrievalMs);
    }

    private static String snippet(String text) {
        String stripped = text.strip();
        return stripped.length() <= 240 ? stripped : stripped.substring(0, 240) + "…";
    }
}

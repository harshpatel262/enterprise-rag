package com.harshpatel.rag.core.llm;

import com.harshpatel.rag.core.store.ScoredChunk;
import java.util.List;

/** Generates a grounded answer from a question plus retrieved context. */
public interface LlmClient {

    String answer(String question, List<ScoredChunk> context);
}

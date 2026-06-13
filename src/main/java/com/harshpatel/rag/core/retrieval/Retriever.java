package com.harshpatel.rag.core.retrieval;

import com.harshpatel.rag.core.store.ScoredChunk;
import java.util.List;

/** Returns the top-k chunks most relevant to a natural-language query. */
public interface Retriever {

    List<ScoredChunk> retrieve(String query, int topK);
}

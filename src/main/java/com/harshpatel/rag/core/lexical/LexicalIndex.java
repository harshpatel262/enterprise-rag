package com.harshpatel.rag.core.lexical;

import com.harshpatel.rag.core.model.DocumentChunk;
import com.harshpatel.rag.core.store.ScoredChunk;
import java.util.List;

/**
 * Keyword (sparse) retrieval over chunk text. Complements dense vector
 * search: lexical matching reliably surfaces exact terms, identifiers,
 * and rare tokens that embeddings tend to blur together.
 */
public interface LexicalIndex {

    void index(List<DocumentChunk> chunks);

    List<ScoredChunk> search(String query, int topK);

    long count();
}

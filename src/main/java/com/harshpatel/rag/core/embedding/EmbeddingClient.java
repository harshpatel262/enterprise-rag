package com.harshpatel.rag.core.embedding;

/** Maps text to a fixed-dimension vector for similarity search. */
public interface EmbeddingClient {

    float[] embed(String text);

    int dimension();
}

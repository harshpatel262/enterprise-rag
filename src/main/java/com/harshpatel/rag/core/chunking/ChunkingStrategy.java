package com.harshpatel.rag.core.chunking;

import java.util.List;

/** Splits raw document text into retrieval-sized chunks. */
public interface ChunkingStrategy {

    List<String> chunk(String text);
}

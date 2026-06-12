package com.harshpatel.rag.api.dto;

import java.util.UUID;

public record IngestResponse(UUID documentId, int chunkCount, long totalIndexedChunks) {
}

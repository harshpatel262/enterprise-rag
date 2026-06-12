package com.harshpatel.rag.api.dto;

import java.util.List;

public record QueryResponse(String answer, List<Citation> citations, long retrievalMs) {
}

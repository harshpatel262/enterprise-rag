package com.harshpatel.rag.api.dto;

public record QueryRequest(String question, Integer topK) {
}

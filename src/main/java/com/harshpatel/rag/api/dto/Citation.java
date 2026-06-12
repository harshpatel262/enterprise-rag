package com.harshpatel.rag.api.dto;

public record Citation(String documentTitle, int sequence, String snippet, double score) {
}

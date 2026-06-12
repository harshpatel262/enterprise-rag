package com.harshpatel.rag.api;

import com.harshpatel.rag.api.dto.IngestRequest;
import com.harshpatel.rag.api.dto.IngestResponse;
import com.harshpatel.rag.service.IngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final IngestionService ingestion;

    public DocumentController(IngestionService ingestion) {
        this.ingestion = ingestion;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngestResponse ingest(@RequestBody IngestRequest request) {
        if (request.title() == null || request.title().isBlank()
                || request.content() == null || request.content().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "title and content are required");
        }
        var result = ingestion.ingest(request.title().strip(), request.content());
        return new IngestResponse(
                result.documentId(), result.chunkCount(), ingestion.indexedChunks());
    }
}

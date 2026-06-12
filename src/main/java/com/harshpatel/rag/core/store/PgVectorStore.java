package com.harshpatel.rag.core.store;

import com.harshpatel.rag.core.model.DocumentChunk;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * pgvector-backed store using an IVF-friendly cosine index
 * (see schema.sql). Activated with the "postgres" profile.
 */
@Component
@Profile("postgres")
public class PgVectorStore implements VectorStore {

    private final JdbcTemplate jdbc;

    public PgVectorStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void upsert(List<DocumentChunk> chunks) {
        for (DocumentChunk chunk : chunks) {
            jdbc.update("""
                    INSERT INTO chunks (id, document_id, document_title, sequence, text, embedding)
                    VALUES (?, ?, ?, ?, ?, ?::vector)
                    ON CONFLICT (id) DO UPDATE
                        SET text = EXCLUDED.text, embedding = EXCLUDED.embedding
                    """,
                    chunk.id(), chunk.documentId(), chunk.documentTitle(),
                    chunk.sequence(), chunk.text(), toVectorLiteral(chunk.embedding()));
        }
    }

    @Override
    public List<ScoredChunk> search(float[] queryEmbedding, int topK) {
        String literal = toVectorLiteral(queryEmbedding);
        return jdbc.query("""
                SELECT id, document_id, document_title, sequence, text,
                       1 - (embedding <=> ?::vector) AS score
                FROM chunks
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """,
                (rs, rowNum) -> new ScoredChunk(
                        new DocumentChunk(
                                rs.getObject("id", UUID.class),
                                rs.getObject("document_id", UUID.class),
                                rs.getString("document_title"),
                                rs.getInt("sequence"),
                                rs.getString("text"),
                                new float[0]), // embeddings stay in the database
                        rs.getDouble("score")),
                literal, literal, topK);
    }

    @Override
    public long count() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM chunks", Long.class);
        return count == null ? 0 : count;
    }

    private static String toVectorLiteral(float[] embedding) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(embedding[i]);
        }
        return builder.append(']').toString();
    }
}

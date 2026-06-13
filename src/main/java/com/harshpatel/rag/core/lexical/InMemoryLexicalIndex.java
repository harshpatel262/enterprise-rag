package com.harshpatel.rag.core.lexical;

import com.harshpatel.rag.core.model.DocumentChunk;
import com.harshpatel.rag.core.store.ScoredChunk;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Brute-force <a href="https://en.wikipedia.org/wiki/Okapi_BM25">BM25</a>
 * ranking over chunk text. Term-frequency saturation (k1) and length
 * normalization (b) use the standard defaults. Consistent with
 * {@code InMemoryVectorStore}, scoring is a linear scan — appropriate
 * into the tens of thousands of chunks; a postings-list inverted index
 * is the scale-up path behind this same interface.
 */
public final class InMemoryLexicalIndex implements LexicalIndex {

    private static final double K1 = 1.2;
    private static final double B = 0.75;
    private static final Pattern TOKEN = Pattern.compile("[a-z0-9]+");

    private record Entry(DocumentChunk chunk, Map<String, Integer> termFreq, int length) {
    }

    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, Integer> documentFrequency = new HashMap<>();
    private long totalLength = 0;

    private static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        var matcher = TOKEN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    @Override
    public synchronized void index(List<DocumentChunk> chunks) {
        for (DocumentChunk chunk : chunks) {
            List<String> tokens = tokenize(chunk.text());
            Map<String, Integer> termFreq = new HashMap<>();
            for (String token : tokens) {
                termFreq.merge(token, 1, Integer::sum);
            }
            for (String term : termFreq.keySet()) {
                documentFrequency.merge(term, 1, Integer::sum);
            }
            entries.add(new Entry(chunk, termFreq, tokens.size()));
            totalLength += tokens.size();
        }
    }

    @Override
    public synchronized List<ScoredChunk> search(String query, int topK) {
        if (entries.isEmpty()) {
            return List.of();
        }
        int n = entries.size();
        double avgLength = (double) totalLength / n;
        List<String> queryTerms = tokenize(query);

        List<ScoredChunk> scored = new ArrayList<>(n);
        for (Entry entry : entries) {
            double score = 0;
            for (String term : queryTerms) {
                Integer freq = entry.termFreq().get(term);
                if (freq == null) {
                    continue;
                }
                int df = documentFrequency.getOrDefault(term, 0);
                double idf = Math.log(1 + (n - df + 0.5) / (df + 0.5));
                double denominator = freq + K1 * (1 - B + B * entry.length() / avgLength);
                score += idf * (freq * (K1 + 1)) / denominator;
            }
            if (score > 0) {
                scored.add(new ScoredChunk(entry.chunk(), score));
            }
        }
        scored.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());
        return scored.subList(0, Math.min(topK, scored.size()));
    }

    @Override
    public synchronized long count() {
        return entries.size();
    }
}

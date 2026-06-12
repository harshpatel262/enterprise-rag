package com.harshpatel.rag.core.embedding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hashing-trick bag-of-words embedder: deterministic, dependency-free,
 * and fast enough to embed at ingest time with zero infrastructure.
 * It captures lexical similarity only — swap in a transformer-based
 * {@link EmbeddingClient} for semantic paraphrase matching; nothing
 * else in the pipeline changes.
 */
public final class HashingEmbeddingClient implements EmbeddingClient {

    private static final int DIMENSION = 256;
    private static final Pattern TOKEN = Pattern.compile("[a-z0-9]+");

    @Override
    public float[] embed(String text) {
        float[] vector = new float[DIMENSION];
        Matcher matcher = TOKEN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            byte[] digest = md5(matcher.group());
            int index = Math.floorMod(toInt(digest), DIMENSION);
            float sign = (digest[4] & 1) == 0 ? 1f : -1f;
            vector[index] += sign;
        }
        normalize(vector);
        return vector;
    }

    @Override
    public int dimension() {
        return DIMENSION;
    }

    private static byte[] md5(String token) {
        try {
            return MessageDigest.getInstance("MD5")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 unavailable", e);
        }
    }

    private static int toInt(byte[] digest) {
        return (digest[0] & 0xFF)
                | (digest[1] & 0xFF) << 8
                | (digest[2] & 0xFF) << 16
                | (digest[3] & 0xFF) << 24;
    }

    private static void normalize(float[] vector) {
        double sum = 0;
        for (float v : vector) {
            sum += v * v;
        }
        double norm = Math.sqrt(sum);
        if (norm == 0) {
            return;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= (float) norm;
        }
    }
}

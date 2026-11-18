package com.practice.nexora.search;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(prefix = "nexora.advisor", name = "provider", havingValue = "offline")
class OfflineEmbeddingClient implements EmbeddingClient {

    static final String MODEL = "offline-word-hashing";

    @Override
    public List<float[]> embed(List<String> texts) {
        return texts.stream().map(OfflineEmbeddingClient::embedOne).toList();
    }

    @Override
    public String modelName() {
        return MODEL;
    }

    static float[] embedOne(String text) {
        float[] vector = new float[DIMENSIONS];
        for (String word : text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
            if (word.length() >= 3) {
                vector[Math.floorMod(word.hashCode(), DIMENSIONS)] += 1f;
            }
        }
        return vector;
    }
}

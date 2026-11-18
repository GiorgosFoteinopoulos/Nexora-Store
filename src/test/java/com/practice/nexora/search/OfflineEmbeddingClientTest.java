package com.practice.nexora.search;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OfflineEmbeddingClientTest {

    private final OfflineEmbeddingClient client = new OfflineEmbeddingClient();

    private static double cosineDistance(float[] a, float[] b) {
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return 1 - dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @Test
    void everyTextBecomesExactly1536Numbers() {
        assertThat(client.embed(List.of("A light laptop", "x")))
                .allSatisfy(vector -> assertThat(vector).hasSize(EmbeddingClient.DIMENSIONS));
    }

    @Test
    void theSameTextAlwaysGivesTheSameVector() {
        assertThat(OfflineEmbeddingClient.embedOne("Light laptop for travel"))
                .isEqualTo(OfflineEmbeddingClient.embedOne("Light laptop for travel"));
    }

    @Test
    void textsSharingWordsAreCloserThanUnrelatedTexts() {
        float[] question = OfflineEmbeddingClient.embedOne("light laptop for travel");
        float[] similar = OfflineEmbeddingClient.embedOne("A very light laptop, great for travel and work");
        float[] unrelated = OfflineEmbeddingClient.embedOne("Kids watch with GPS location for parents");

        assertThat(cosineDistance(question, similar)).isLessThan(cosineDistance(question, unrelated));
    }
}

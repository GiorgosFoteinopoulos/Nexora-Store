package com.practice.nexora.search;

import java.util.List;

public interface EmbeddingClient {

    int DIMENSIONS = 1536;

    List<float[]> embed(List<String> texts);

    String modelName();

    static float[] requireDimensions(float[] vector) {
        if (vector.length != DIMENSIONS) {
            throw new IllegalStateException(
                    "Embedding has %d numbers, but the database expects %d. Use an embedding model with %d dimensions."
                            .formatted(vector.length, DIMENSIONS, DIMENSIONS)
                );
        }
        return vector;
    }
}

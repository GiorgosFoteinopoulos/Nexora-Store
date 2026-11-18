package com.practice.nexora.search;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "nexora.advisor", name = "provider", havingValue = "openai")
class OpenAiEmbeddingClient implements EmbeddingClient {

    private final EmbeddingModel model;
    private final String modelName;

    OpenAiEmbeddingClient(EmbeddingModel model, Environment environment) {
        this.model = model;
        this.modelName = environment.getProperty("spring.ai.openai.embedding.model", "openai-embedding");
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        List<float[]> vectors = model.embed(texts);
        vectors.forEach(EmbeddingClient::requireDimensions);
        return vectors;
    }

    @Override
    public String modelName() {
        return modelName;
    }
}

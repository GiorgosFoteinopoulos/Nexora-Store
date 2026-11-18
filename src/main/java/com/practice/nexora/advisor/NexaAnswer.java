package com.practice.nexora.advisor;

import java.util.List;

public record NexaAnswer(String reply, List<String> recommendedSlugs) {

    public NexaAnswer {
        reply = reply == null ? "" : reply;
        recommendedSlugs = recommendedSlugs == null ? List.of() : List.copyOf(recommendedSlugs);
    }
}

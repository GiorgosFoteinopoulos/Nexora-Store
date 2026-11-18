package com.practice.nexora.search;

import java.util.List;

public record SemanticSearchResponse(String query, List<SemanticHit> hits) {

}

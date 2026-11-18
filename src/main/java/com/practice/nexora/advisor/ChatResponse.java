package com.practice.nexora.advisor;

import com.practice.nexora.catalog.ProductResponse;

import java.util.List;

public record ChatResponse(String reply, List<ProductResponse> products) {
}

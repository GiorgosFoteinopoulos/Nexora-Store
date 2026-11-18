package com.practice.nexora.advisor;


import com.practice.nexora.catalog.ProductResponse;
import com.practice.nexora.catalog.ProductService;
import com.practice.nexora.common.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AdvisorService {

    private static final Logger log = LoggerFactory.getLogger(AdvisorService.class);

    static final int MAX_RECOMMENDATIONS = 3;

    private final AdvisorClient advisor;
    private final ProductService products;

    public AdvisorService(AdvisorClient advisor, ProductService products) {
        this.advisor = advisor;
        this.products = products;
    }

    public ChatResponse chat(String message) {
        NexaAnswer answer = advisor.reply(message);

        List<ProductResponse> cards = answer.recommendedSlugs().stream()
                .map(slug -> slug == null ? "" : slug.strip().toLowerCase(Locale.ROOT))
                .filter(slug -> !slug.isEmpty())
                .distinct()
                .map(this::findRealProduct)
                .flatMap(Optional::stream)
                .limit(MAX_RECOMMENDATIONS)
                .toList();

        return new ChatResponse(answer.reply(), cards);
    }

    private Optional<ProductResponse> findRealProduct(String slug) {
        try {
            return Optional.of(products.getProduct(slug));
        } catch (NotFoundException invented) {
            log.warn("Nexa recommended a product that does not exist: '{}' (dropped)", slug);
            return Optional.empty();
        }
    }
}

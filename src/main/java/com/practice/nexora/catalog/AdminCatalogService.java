package com.practice.nexora.catalog;


import com.practice.nexora.common.ConflictException;
import com.practice.nexora.common.CurrentUser;
import com.practice.nexora.common.Money;
import com.practice.nexora.common.NotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdminCatalogService {

    private final ProductRepository products;
    private final PriceChangeRepository priceChanges;

    public AdminCatalogService(ProductRepository products, PriceChangeRepository priceChanges) {
        this.products = products;
        this.priceChanges = priceChanges;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public AdminVariantResponse getVariant(String sku) {
        return AdminVariantResponse.from(find(sku));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<PriceChangeResponse> getPriceHistory(String sku) {
        Sku existing = find(sku).getSku();
        return priceChanges.findByVariant_SkuOrderByChangedAtAscIdAsc(existing).stream()
                .map(PriceChangeResponse::from)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AdminVariantResponse updateVariant(String sku, UpdateVariantRequest request) {
        ProductVariant variant = find(sku);

        if (variant.getVersion() != request.version()) {
            throw new ConflictException("This variant was changed by someone else (now version %d, you sent %d). Reload and try again."
                    .formatted(variant.getVersion(), request.version()));
        }
        if (request.price() != null) {
            Money newPrice = new Money(request.price(), variant.getPrice().currency());
            if (!newPrice.equals(variant.getPrice())) {
                priceChanges.save(new PriceChange(variant, variant.getPrice(), newPrice, CurrentUser.name()));
                variant.changePrice(newPrice);
            }
        }
        if (request.stock() != null) {
            setStock(variant, request.stock());
        }

        products.flush();
        return AdminVariantResponse.from(variant);
    }


    private static void setStock(ProductVariant variant, int target) {
        int current = variant.getStock();
        if (target > current) {
            variant.restock(target - current);
        } else if (target < current) {
            variant.removeFromStock(current - target);
        }
    }


    private ProductVariant find(String sku) {
        return parse(sku)
                .flatMap(products::findVariantBySku)
                .orElseThrow(() -> new NotFoundException("No variant with SKU '%s'".formatted(sku)));
    }

    private static Optional<Sku> parse(String value) {
        try {
            return Optional.of(new Sku(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

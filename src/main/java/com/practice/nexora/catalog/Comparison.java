package com.practice.nexora.catalog;

import com.practice.nexora.common.Money;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public final class Comparison {

    public static final int MIN_PRODUCTS = 2;
    public static final int MAX_PRODUCTS = 4;

    private final List<Product> products;

    public Comparison(List<Product> products) {
        Objects.requireNonNull(products, "products must not be null");
        List<Product> copy = List.copyOf(products);

        if (copy.size() < MIN_PRODUCTS || copy.size() > MAX_PRODUCTS) {
            throw new IllegalArgumentException(
                    "A comparison needs %d to %d products, got %d"
                            .formatted(MIN_PRODUCTS, MAX_PRODUCTS, copy.size()));
        }
        if (new HashSet<>(copy).size() != copy.size()) {
            throw new IllegalArgumentException("The same product appears more than once");
        }
        Category category = copy.getFirst().getCategory();
        if (copy.stream().anyMatch(product -> product.getCategory() != category)) {
            throw new IllegalArgumentException("Only products of the same category can be compared");
        }
        this.products = copy;
    }

    public List<Product> getProducts() {
        return products; // already unmodifiable (List.copyOf)
    }

    public Category getCategory() {
        return products.getFirst().getCategory();
    }


    public Map<String, List<String>> table() {
        Map<String, List<String>> rows = new LinkedHashMap<>();
        rows.put("Product", column(Product::getName));
        rows.put("Brand", column(product -> product.getBrand().getName()));
        rows.put("Price from", column(product ->
                product.getStartingPrice().map(Comparison::format).orElse("-")));
        rows.put("Available", column(product -> yesNo(product.isAvailable())));

        List<List<Spec>> specs = products.stream().map(Comparison::specsOf).toList();
        List<Spec> firstProductSpecs = specs.getFirst();
        for (int row = 0; row < firstProductSpecs.size(); row++) {
            int index = row;
            rows.put(firstProductSpecs.get(row).label(),
                    specs.stream().map(productSpecs -> productSpecs.get(index).value()).toList());
        }
        return Collections.unmodifiableMap(rows);
    }


    public Optional<Product> cheapest() {
        return products.stream()
                .filter(product -> product.getStartingPrice().isPresent())
                .min(Comparator.comparing(product -> product.getStartingPrice().orElseThrow().amount()));
    }



    private record Spec(String label, String value) {
    }

    private static List<Spec> specsOf(Product product) {
        return switch (product) {
            case Phone phone -> List.of(
                    new Spec("Screen", phone.getScreenInches() + "\""),
                    new Spec("Battery", phone.getBatteryMah() + " mAh"),
                    new Spec("5G", yesNo(phone.isFiveG())));
            case Laptop laptop -> List.of(
                    new Spec("Screen", laptop.getScreenInches() + "\""),
                    new Spec("Processor", laptop.getProcessor()),
                    new Spec("Memory", laptop.getRamGb() + " GB"),
                    new Spec("Weight", laptop.getWeightKg() + " kg"));
            case Tablet tablet -> List.of(
                    new Spec("Screen", tablet.getScreenInches() + "\""),
                    new Spec("Stylus", yesNo(tablet.hasStylusSupport())),
                    new Spec("Cellular", yesNo(tablet.isCellular())));
            case Wearable wearable -> List.of(
                    new Spec("Battery life", wearable.getBatteryLifeDays() + " days"),
                    new Spec("Heart rate", yesNo(wearable.hasHeartRateMonitor())),
                    new Spec("GPS", yesNo(wearable.hasGps())));
            case Accessory accessory -> List.of(
                    new Spec("Fits", accessory.getCompatibleWith().stream()
                            .map(Category::getDisplayName)
                            .collect(Collectors.joining(", "))));
            default -> throw new IllegalStateException(
                    "Unknown kind of product: " + product.getClass().getSimpleName());
        };
    }

    private List<String> column(Function<Product, String> cell) {
        return products.stream().map(cell).toList();
    }

    private static String format(Money money) {
        return money.amount().toPlainString() + " " + money.currency().getCurrencyCode();
    }

    private static String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }
}
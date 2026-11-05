package com.practice.nexora.catalog;

import static com.practice.nexora.common.Numbers.requireBetween;
import static com.practice.nexora.common.Numbers.requirePositive;
import static com.practice.nexora.common.Text.requireText;

public class Laptop extends Product {

    private static final double ULTRAPORTABLE_LIMIT_KG = 1.3;

    private final double screenInches;
    private final String processor;
    private final int ramGb;
    private final double weightKg;

    public Laptop(String name, Brand brand, String description,
                  double screenInches, String processor, int ramGb, double weightKg) {
        super(name, brand, description);
        this.screenInches = requireBetween(screenInches, 10.0, 18.0, "screenInches");
        this.processor = requireText(processor, "processor");
        this.ramGb = requirePositive(ramGb, "ramGb");
        this.weightKg = requireBetween(weightKg, 0.5, 5.0, "weightKg");
    }

    @Override
    public Category getCategory() {
        return Category.LAPTOP;
    }

    public boolean isUltraportable() {
        return weightKg < ULTRAPORTABLE_LIMIT_KG;
    }

    public double getScreenInches() {
        return screenInches;
    }
    public String getProcessor() {
        return processor;
    }
    public int getRamGb() {
        return ramGb;
    }
    public double getWeightKg() {
        return weightKg;
    }
}

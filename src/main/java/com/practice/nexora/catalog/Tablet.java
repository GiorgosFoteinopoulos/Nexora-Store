package com.practice.nexora.catalog;

import static com.practice.nexora.common.Numbers.requireBetween;


public class Tablet extends Product {

    private final double screenInches;
    private final boolean stylusSupport;
    private final boolean cellular;

    public Tablet(String name, Brand brand, String description,
                  double screenInches, boolean stylusSupport, boolean cellular) {
        super(name, brand, description);
        this.screenInches = requireBetween(screenInches, 7.0, 14.0, "screenInches");
        this.stylusSupport = stylusSupport;
        this.cellular = cellular;
    }

    @Override
    public Category getCategory() {
        return Category.TABLET;
    }

    public double getScreenInches() {
        return screenInches;
    }

    public boolean hasStylusSupport() {
        return stylusSupport;
    }

    public boolean isCellular() {
        return cellular;
    }
}
package com.practice.nexora.catalog;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import static com.practice.nexora.common.Numbers.requireBetween;

@Entity
@Table(name = "tablets")
@DiscriminatorValue("TABLET")
public class Tablet extends Product {

    private double screenInches;
    private boolean stylusSupport;
    private boolean cellular;

    protected Tablet() {

    }

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
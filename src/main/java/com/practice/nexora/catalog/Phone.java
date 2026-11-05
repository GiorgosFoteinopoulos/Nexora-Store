package com.practice.nexora.catalog;

public class Phone extends Product{

    private final double screenInches;
    private final int batteryMah;
    private final boolean fiveG;

    public Phone(String name, Brand brand, String description,
                 double screenInches, int batteryMah, boolean fiveG) {
        super(name, brand, description);
        if (!(screenInches >= 3.0 && screenInches <= 8.0)) {
            throw new IllegalArgumentException("screenInches must be between 3.0 and 8.0: " + screenInches);
        }
        if (batteryMah <= 0) {
            throw new IllegalArgumentException("batteryMah must be positive: " + batteryMah);
        }
        this.screenInches = screenInches;
        this.batteryMah = batteryMah;
        this.fiveG = fiveG;
    }

    public Category getCategory() {
        return Category.PHONE;
    }
    public double getScreenInches() {
        return screenInches;
    }
    public int getBatteryMah() {
        return batteryMah;
    }

    public boolean isFiveG() {
        return fiveG;
    }
}

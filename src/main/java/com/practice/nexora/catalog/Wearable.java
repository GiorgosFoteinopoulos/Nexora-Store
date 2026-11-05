package com.practice.nexora.catalog;

import static com.practice.nexora.common.Numbers.requirePositive;


public class Wearable extends Product {

    private final int batteryLifeDays;
    private final boolean heartRateMonitor;
    private final boolean gps;

    public Wearable(String name, Brand brand, String description,
                    int batteryLifeDays, boolean heartRateMonitor, boolean gps) {
        super(name, brand, description);
        this.batteryLifeDays = requirePositive(batteryLifeDays, "batteryLifeDays");
        this.heartRateMonitor = heartRateMonitor;
        this.gps = gps;
    }

    @Override
    public Category getCategory() {
        return Category.WEARABLE;
    }

    public int getBatteryLifeDays() {
        return batteryLifeDays;
    }

    public boolean hasHeartRateMonitor() {
        return heartRateMonitor;
    }

    public boolean hasGps() {
        return gps;
    }
}
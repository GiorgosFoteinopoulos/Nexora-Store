package com.practice.nexora.catalog;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import static com.practice.nexora.common.Numbers.requirePositive;

@Entity
@Table(name = "wearables")
@DiscriminatorValue("WEARABLE")
public class Wearable extends Product {

    private  int batteryLifeDays;
    private  boolean heartRateMonitor;
    private  boolean gps;

    protected Wearable() {

    }

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
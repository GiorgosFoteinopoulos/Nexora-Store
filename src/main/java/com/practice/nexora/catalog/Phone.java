package com.practice.nexora.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import static com.practice.nexora.common.Numbers.requireBetween;
import static com.practice.nexora.common.Numbers.requirePositive;

@Entity
@Table(name = "phones")
@DiscriminatorValue("PHONE")
public class Phone extends Product{

    private double screenInches;
    private int batteryMah;
    @Column(name = "five_g")
    private boolean fiveG;

    protected Phone() {

    }

    public Phone(String name, Brand brand, String description,
                 double screenInches, int batteryMah, boolean fiveG) {
        super(name, brand, description);
       this.screenInches = requireBetween(screenInches, 3.0, 8.0, "screenInches");
       this.batteryMah = requirePositive(batteryMah, "batteryMah");
       this.fiveG = fiveG;
    }

    @Override
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

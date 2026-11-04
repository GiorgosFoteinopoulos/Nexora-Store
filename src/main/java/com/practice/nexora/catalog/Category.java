package com.practice.nexora.catalog;

public enum Category {

    PHONE("Phones"),
    LAPTOP("Laptops"),
    TABLET("Tablets"),
    WEARABLE("Wearables"),
    ACCESSORY("Accessories");

    private final String displayName;
    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

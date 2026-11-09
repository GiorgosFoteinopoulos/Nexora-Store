package com.practice.nexora.catalog;

import java.util.List;
import java.util.stream.Collectors;

public sealed interface ProductSpecs {

    record  PhoneSpecs(double screenInches, int batteryMah, boolean fiveG) implements ProductSpecs {
    }

    record LaptopSpecs(double screenInches, String processor, int ramGb, double weightKg,
                       boolean ultraportable) implements ProductSpecs {
    }

    record TabletSpecs(double screenInches, boolean stylusSupport, boolean cellular) implements ProductSpecs {

    }

    record WearableSpecs(int batteryLifeDays, boolean heartRateMonitor, boolean gps) implements ProductSpecs {

    }

    record AccessorySpecs(List<Category> fits) implements ProductSpecs {

    }
    

    static ProductSpecs of(Product product) {
        return switch (product) {
            case Phone phone -> new PhoneSpecs(phone.getScreenInches(), phone.getBatteryMah(), phone.isFiveG());
            case Laptop laptop -> new LaptopSpecs(laptop.getScreenInches(), laptop.getProcessor(),
                    laptop.getRamGb(), laptop.getWeightKg(), laptop.isUltraportable());
            case Tablet tablet -> new TabletSpecs(tablet.getScreenInches(), tablet.hasStylusSupport(),
                    tablet.isCellular());
            case Wearable wearable -> new WearableSpecs(wearable.getBatteryLifeDays(),
                    wearable.hasHeartRateMonitor(), wearable.hasGps());
            case Accessory accessory -> new AccessorySpecs(
                    accessory.getCompatibleWith().stream().sorted().toList()); // Hibernate's Set has no order
            default -> throw new IllegalStateException(
                    "Unknown kind of product: " + product.getClass().getSimpleName());
        };
    }

    default String summary() {
        return switch (this) {
            case PhoneSpecs phone -> "%s\" screen, %d mAh%s".formatted(
                    phone.screenInches(), phone.batteryMah(), phone.fiveG() ? ", 5G" : "");
            case LaptopSpecs laptop -> "%s\", %s, %d GB RAM, %s kg".formatted(
                    laptop.screenInches(), laptop.processor(), laptop.ramGb(), laptop.weightKg());
            case TabletSpecs tablet -> "%s\" screen%s%s".formatted(
                    tablet.screenInches(), tablet.stylusSupport() ? ", stylus" : "", tablet.cellular() ? ", cellular" : "");
            case WearableSpecs wearable -> "%d days battery%s%s".formatted(
                    wearable.batteryLifeDays(), wearable.heartRateMonitor() ? ", heart rate" : "", wearable.gps() ? ", GPS" : "");
            case AccessorySpecs accessory -> "Fits " + accessory.fits().stream()
                    .map(Category::getDisplayName)
                    .collect(Collectors.joining(", "));
        };
    }
}
package com.practice.nexora.catalog;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProductKindsTest {

    private static final Brand NEXORA = new Brand("Nexora");

    private static Laptop laptop(double screenInches, String processor, double weightKg) {
        return new Laptop("Nexora Book 14", NEXORA, "Text", screenInches, processor, 16, weightKg);
    }

    private static Phone phone() {
        return new Phone("Nexora Phone 15", NEXORA, "Text", 6.1, 4500, true);
    }

    @Nested
    class LaptopRules {

        @Test
        void keepsItsSpecifications() {
            Laptop laptop = laptop(14.0, "Nexora N3", 1.2);

            assertThat(laptop.getCategory()).isEqualTo(Category.LAPTOP);
            assertThat(laptop.getScreenInches()).isEqualTo(14.0);
            assertThat(laptop.getProcessor()).isEqualTo("Nexora N3");
            assertThat(laptop.getRamGb()).isEqualTo(16);
            assertThat(laptop.getWeightKg()).isEqualTo(1.2);
        }

        @ParameterizedTest(name = "{0} kg -> ultraportable: {1}")
        @CsvSource({"1.29, true", "1.30, false"})
        void isUltraportableBelowTheWeightLimit(double weightKg, boolean expected) {
            assertThat(laptop(14.0, "Nexora N3", weightKg).isUltraportable()).isEqualTo(expected);
        }

        @Test
        void rejectsAScreenOutsideTheLaptopRange() {
            assertThatThrownBy(() -> laptop(9.9, "Nexora N3", 1.2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("screenInches");
        }

        @Test
        void rejectsAnImpossibleWeight() {
            assertThatThrownBy(() -> laptop(14.0, "Nexora N3", 5.1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("weightKg");
        }

        @Test
        void rejectsABlankProcessor() {
            assertThatThrownBy(() -> laptop(14.0, "  ", 1.2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("processor must not be blank");
        }
    }

    @Nested
    class TabletRules {

        @Test
        void keepsItsSpecifications() {
            Tablet tablet = new Tablet("Nexora Tab 11", NEXORA, "Text", 11.0, true, false);

            assertThat(tablet.getCategory()).isEqualTo(Category.TABLET);
            assertThat(tablet.getScreenInches()).isEqualTo(11.0);
            assertThat(tablet.hasStylusSupport()).isTrue();
            assertThat(tablet.isCellular()).isFalse();
        }

        @ParameterizedTest(name = "rejects {0} inches")
        @ValueSource(doubles = {6.9, 14.1})
        void rejectsAScreenOutsideTheTabletRange(double inches) {
            assertThatThrownBy(() -> new Tablet("Nexora Tab 11", NEXORA, "Text", inches, true, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("screenInches");
        }
    }

    @Nested
    class WearableRules {

        @Test
        void keepsItsSpecifications() {
            Wearable watch = new Wearable("Nexora Watch S3", NEXORA, "Text", 7, true, true);

            assertThat(watch.getCategory()).isEqualTo(Category.WEARABLE);
            assertThat(watch.getBatteryLifeDays()).isEqualTo(7);
            assertThat(watch.hasHeartRateMonitor()).isTrue();
            assertThat(watch.hasGps()).isTrue();
        }

        @Test
        void rejectsNonPositiveBatteryLife() {
            assertThatThrownBy(() -> new Wearable("Nexora Watch S3", NEXORA, "Text", 0, true, true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("batteryLifeDays");
        }
    }

    @Nested
    class AccessoryRules {

        @Test
        void fitsOnlyTheCategoriesItWasMadeFor() {
            Accessory charger = new Accessory("Nexora 30W Charger", NEXORA, "Text",
                    Set.of(Category.PHONE, Category.TABLET));

            assertThat(charger.getCategory()).isEqualTo(Category.ACCESSORY);
            assertThat(charger.fits(phone())).isTrue();
            assertThat(charger.fits(laptop(14.0, "Nexora N3", 1.2))).isFalse();
        }

        @Test
        void isNotAffectedByLaterChangesToTheCallersSet() {
            Set<Category> callersSet = new HashSet<>(Set.of(Category.PHONE));
            Accessory phoneCase = new Accessory("Nexora Phone 15 Case", NEXORA, "Text", callersSet);

            callersSet.add(Category.LAPTOP); // the caller changes its own set afterwards

            assertThat(phoneCase.fits(laptop(14.0, "Nexora N3", 1.2))).isFalse();
            assertThat(phoneCase.getCompatibleWith()).containsExactly(Category.PHONE);
        }

        @Test
        void itsCompatibilityCannotBeChangedFromOutside() {
            Accessory phoneCase = new Accessory("Nexora Phone 15 Case", NEXORA, "Text",
                    Set.of(Category.PHONE));

            assertThatThrownBy(() -> phoneCase.getCompatibleWith().add(Category.LAPTOP))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void mustFitAtLeastOneCategory() {
            assertThatThrownBy(() -> new Accessory("Nexora Cable", NEXORA, "Text", Set.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one category");
        }

        @Test
        void cannotBeMadeForOtherAccessories() {
            assertThatThrownBy(() -> new Accessory("Nexora Cable", NEXORA, "Text",
                    Set.of(Category.PHONE, Category.ACCESSORY)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("other accessories");
        }
    }
}
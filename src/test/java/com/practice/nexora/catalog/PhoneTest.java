package com.practice.nexora.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Only what is specific to phones; shared behaviour is covered by ProductTest. */
class PhoneTest {

    private static final Brand NEXORA = new Brand("Nexora");

    private static Phone phoneWith(double screenInches, int batteryMah) {
        return new Phone("Nexora Phone 15", NEXORA, "Text", screenInches, batteryMah, true);
    }

    @Test
    void keepsItsSpecifications() {
        Phone phone = new Phone("Nexora Phone 15", NEXORA, "Text", 6.1, 4500, false);

        assertThat(phone.getScreenInches()).isEqualTo(6.1);
        assertThat(phone.getBatteryMah()).isEqualTo(4500);
        assertThat(phone.isFiveG()).isFalse();
    }

    @ParameterizedTest(name = "accepts {0} inches")
    @ValueSource(doubles = {3.0, 8.0})
    void acceptsScreenSizesAtTheLimits(double inches) {
        assertThat(phoneWith(inches, 4500).getScreenInches()).isEqualTo(inches);
    }

    @ParameterizedTest(name = "rejects {0} inches")
    @ValueSource(doubles = {2.9, 8.1, -1.0, Double.NaN})
    void rejectsImpossibleScreenSizes(double inches) {
        assertThatThrownBy(() -> phoneWith(inches, 4500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("screenInches");
    }

    @ParameterizedTest(name = "rejects {0} mAh")
    @ValueSource(ints = {0, -1})
    void rejectsNonPositiveBattery(int batteryMah) {
        assertThatThrownBy(() -> phoneWith(6.1, batteryMah))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("batteryMah");
    }
}
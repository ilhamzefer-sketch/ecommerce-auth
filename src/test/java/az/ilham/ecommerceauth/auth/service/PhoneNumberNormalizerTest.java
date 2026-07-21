package az.ilham.ecommerceauth.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneNumberNormalizerTest {

    private final PhoneNumberNormalizer normalizer = new PhoneNumberNormalizer();

    @Test
    void normalizesAzerbaijanLocalAndFormattedNumbers() {
        assertEquals("+994501234567", normalizer.normalize("050 123 45 67"));
        assertEquals("+994501234567", normalizer.normalize("+994 (50) 123-45-67"));
    }

    @Test
    void rejectsInvalidNumber() {
        assertThrows(IllegalArgumentException.class, () -> normalizer.normalize("123"));
    }
}

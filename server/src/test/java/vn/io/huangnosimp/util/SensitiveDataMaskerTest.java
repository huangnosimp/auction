package vn.io.huangnosimp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SensitiveDataMaskerTest {
    @Test
    void maskEmailKeepsOnlyFirstCharacterAndDomain() {
        assertEquals("u***@example.com", SensitiveDataMasker.maskEmail("user@example.com"));
    }

    @Test
    void maskEmailHandlesInvalidInput() {
        assertEquals("", SensitiveDataMasker.maskEmail(null));
        assertEquals("", SensitiveDataMasker.maskEmail(""));
        assertEquals("***", SensitiveDataMasker.maskEmail("not-an-email"));
    }

    @Test
    void maskSecretNeverReturnsOriginalValue() {
        assertEquals("***", SensitiveDataMasker.maskSecret("password"));
        assertEquals("", SensitiveDataMasker.maskSecret(null));
    }
}

package com.usermonitor.server.config;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SecuritySettingsValidatorTest {
    @Test void rejectsMissingShortAndExampleCredentials() {
        for (String value : new String[]{null, "", "short", "ChangeThisImmediately!", "replace-with-a-long-random-secret"}) {
            assertThrows(IllegalStateException.class, () -> SecuritySettingsValidator.requireSecret("TEST", value, 16));
        }
    }
    @Test void acceptsGeneratedCredentialsWithoutEchoingSecrets() {
        assertDoesNotThrow(() -> SecuritySettingsValidator.requireSecret("TEST", "e9d0a2608b749a80f6e328df732c57ba", 32));
        String secret = "short";
        var error = assertThrows(IllegalStateException.class, () -> SecuritySettingsValidator.requireSecret("TEST", secret, 32));
        assertFalse(error.getMessage().contains(secret));
    }
}
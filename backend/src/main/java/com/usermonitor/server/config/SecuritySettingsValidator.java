package com.usermonitor.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Locale;

/** Fail closed: examples must never become usable production credentials. */
@Component
public class SecuritySettingsValidator {
    public SecuritySettingsValidator(
            @Value("${app.admin.password}") String password,
            @Value("${app.client-secret}") String secret,
            @Value("${spring.datasource.password}") String databasePassword) {
        requireSecret("ADMIN_PASSWORD", password, 16);
        requireSecret("CLIENT_SECRET", secret, 32);
        requireSecret("DB_PASSWORD", databasePassword, 16);
    }

    static void requireSecret(String name, String value, int minimumLength) {
        String lower = value == null ? "" : value.toLowerCase(Locale.ROOT);
        if (value == null || value.isBlank() || value.length() < minimumLength
                || lower.contains("change") || lower.contains("replace")
                || lower.contains("example") || lower.contains("生成") || lower.contains("随机")) {
            throw new IllegalStateException(name + " must be a unique generated secret (minimum "
                    + minimumLength + " characters)");
        }
    }
}

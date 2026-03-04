package de.hofmannhbm.replay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "replay")
public record ReplayProperties(
        Boolean dashboardEnabled,
        Boolean enabled,
        int maxCapturedRequests,
        List<String> excludePaths,
        List<String> excludeHeaders
) {
    // Standardwerte via Java 21 Record-Konstruktor
    public ReplayProperties {
        if (dashboardEnabled == null) dashboardEnabled = false;
        if (enabled == null) enabled = true;
        if (maxCapturedRequests <= 0) maxCapturedRequests = 100;
        if (excludePaths == null) excludePaths = List.of("/actuator/**", "/favicon.ico");
        if (excludeHeaders == null) excludeHeaders = List.of("Authorization", "Cookie", "X-Api-Key");
    }
}
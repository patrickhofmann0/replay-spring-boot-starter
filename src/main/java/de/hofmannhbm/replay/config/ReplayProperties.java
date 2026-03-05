package de.hofmannhbm.replay.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "replay")
public record ReplayProperties(
        Boolean dashboardEnabled,
        Boolean enabled,
        @Min(0) int maxCapturedRequests,
        List<String> excludePaths,
        @NotNull List<String> excludeHeaders
) {
    // Standardwerte via Java 21 Record-Konstruktor
    public ReplayProperties {
        if (dashboardEnabled == null) dashboardEnabled = false;
        if (enabled == null) enabled = true;
        if (maxCapturedRequests <= 0) maxCapturedRequests = 100;
        if (excludePaths == null) excludePaths = List.of("/actuator/**", "/favicon.ico");
        if (excludeHeaders == null) {
            excludeHeaders = List.of(
                "Authorization", 
                "Cookie", 
                "Set-Cookie",
                "X-Api-Key",
                "Proxy-Authorization",
                "WWW-Authenticate",
                "X-Auth-Token",
                "X-CSRF-Token"
            );
        }
    }
}
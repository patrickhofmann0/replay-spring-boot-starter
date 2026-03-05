package de.hofmannhbm.replay.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReplayPropertiesTest {

    @Test
    void shouldUseDefaultValuesWhenNullProvided() {
        ReplayProperties props = new ReplayProperties(null, null, 0, null, null, null, null);

        assertThat(props.dashboardEnabled()).isFalse();
        assertThat(props.enabled()).isTrue();
        assertThat(props.maxCapturedRequests()).isEqualTo(100);
        assertThat(props.excludePaths()).containsExactly("/actuator/**", "/favicon.ico");
        assertThat(props.excludeHeaders()).containsExactly(
                "Authorization", "Cookie", "Set-Cookie", "X-Api-Key",
                "Proxy-Authorization", "WWW-Authenticate", "X-Auth-Token", "X-CSRF-Token"
        );
        assertThat(props.includeContentTypes()).containsExactly(
                "application/json", "application/xml", "text/plain", "text/html", "application/x-www-form-urlencoded"
        );
        assertThat(props.dashboardPath()).isEqualTo("/replay");
    }

    @Test
    void shouldUseProvidedValues() {
        List<String> customPaths = List.of("/custom/**", "/internal/**");
        List<String> customHeaders = List.of("X-Secret", "X-Token");
        List<String> customContentTypes = List.of("application/json", "text/plain");

        ReplayProperties props = new ReplayProperties(
                true, false, 50, customPaths, customHeaders, customContentTypes, "/custom-replay"
        );

        assertThat(props.dashboardEnabled()).isTrue();
        assertThat(props.enabled()).isFalse();
        assertThat(props.maxCapturedRequests()).isEqualTo(50);
        assertThat(props.excludePaths()).isEqualTo(customPaths);
        assertThat(props.excludeHeaders()).isEqualTo(customHeaders);
        assertThat(props.includeContentTypes()).isEqualTo(customContentTypes);
        assertThat(props.dashboardPath()).isEqualTo("/custom-replay");
    }

    @Test
    void shouldDefaultMaxCapturedRequestsWhenZero() {
        ReplayProperties props = new ReplayProperties(true, true, 0, null, null, null, null);

        assertThat(props.maxCapturedRequests()).isEqualTo(100);
    }

    @Test
    void shouldDefaultMaxCapturedRequestsWhenNegative() {
        ReplayProperties props = new ReplayProperties(true, true, -10, null, null, null, null);

        assertThat(props.maxCapturedRequests()).isEqualTo(100);
    }

    @Test
    void shouldAcceptValidMaxCapturedRequests() {
        ReplayProperties props = new ReplayProperties(true, true, 500, null, null, null, null);

        assertThat(props.maxCapturedRequests()).isEqualTo(500);
    }

    @Test
    void shouldHandleEmptyExcludeLists() {
        ReplayProperties props = new ReplayProperties(
                true, true, 100, List.of(), List.of(), List.of(), "/replay"
        );

        assertThat(props.excludePaths()).isEmpty();
        assertThat(props.excludeHeaders()).isEmpty();
        assertThat(props.includeContentTypes()).isEmpty();
    }

    @Test
    void shouldAllowMixedConfiguration() {
        ReplayProperties props = new ReplayProperties(
                true, null, 200, List.of("/health"), null, null, null
        );

        assertThat(props.dashboardEnabled()).isTrue();
        assertThat(props.enabled()).isTrue(); // default
        assertThat(props.maxCapturedRequests()).isEqualTo(200);
        assertThat(props.excludePaths()).containsExactly("/health");
        assertThat(props.excludeHeaders()).containsExactly(
                "Authorization", "Cookie", "Set-Cookie", "X-Api-Key",
                "Proxy-Authorization", "WWW-Authenticate", "X-Auth-Token", "X-CSRF-Token"
        ); // default
    }
}



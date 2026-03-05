package de.hofmannhbm.replay.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReplayPropertiesTest {

    @Test
    void shouldUseDefaultValuesWhenNullProvided() {
        ReplayProperties props = new ReplayProperties(null, null, 0, null, null);

        assertThat(props.dashboardEnabled()).isFalse();
        assertThat(props.enabled()).isTrue();
        assertThat(props.maxCapturedRequests()).isEqualTo(100);
        assertThat(props.excludePaths()).containsExactly("/actuator/**", "/favicon.ico");
        assertThat(props.excludeHeaders()).containsExactly(
                "Authorization", "Cookie", "Set-Cookie", "X-Api-Key",
                "Proxy-Authorization", "WWW-Authenticate", "X-Auth-Token", "X-CSRF-Token"
        );
    }

    @Test
    void shouldUseProvidedValues() {
        List<String> customPaths = List.of("/custom/**", "/internal/**");
        List<String> customHeaders = List.of("X-Secret", "X-Token");

        ReplayProperties props = new ReplayProperties(
                true, false, 50, customPaths, customHeaders
        );

        assertThat(props.dashboardEnabled()).isTrue();
        assertThat(props.enabled()).isFalse();
        assertThat(props.maxCapturedRequests()).isEqualTo(50);
        assertThat(props.excludePaths()).isEqualTo(customPaths);
        assertThat(props.excludeHeaders()).isEqualTo(customHeaders);
    }

    @Test
    void shouldDefaultMaxCapturedRequestsWhenZero() {
        ReplayProperties props = new ReplayProperties(true, true, 0, null, null);

        assertThat(props.maxCapturedRequests()).isEqualTo(100);
    }

    @Test
    void shouldDefaultMaxCapturedRequestsWhenNegative() {
        ReplayProperties props = new ReplayProperties(true, true, -10, null, null);

        assertThat(props.maxCapturedRequests()).isEqualTo(100);
    }

    @Test
    void shouldAcceptValidMaxCapturedRequests() {
        ReplayProperties props = new ReplayProperties(true, true, 500, null, null);

        assertThat(props.maxCapturedRequests()).isEqualTo(500);
    }

    @Test
    void shouldHandleEmptyExcludeLists() {
        ReplayProperties props = new ReplayProperties(
                true, true, 100, List.of(), List.of()
        );

        assertThat(props.excludePaths()).isEmpty();
        assertThat(props.excludeHeaders()).isEmpty();
    }

    @Test
    void shouldAllowMixedConfiguration() {
        ReplayProperties props = new ReplayProperties(
                true, null, 200, List.of("/health"), null
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



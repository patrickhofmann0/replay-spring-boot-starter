package io.github.patrickhofmann0.replay.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CapturedRequest record.
 */
class CapturedRequestTest {

    @Test
    void shouldCreateCapturedRequestWithAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> headers = Map.of("Content-Type", "application/json");

        // When
        CapturedRequest request = new CapturedRequest(
                "test-id",
                "POST",
                "/api/users",
                "id=123",
                headers,
                "{\"name\":\"John\"}",
                200,
                "{\"id\":\"123\",\"name\":\"John\"}",
                now
        );

        // Then
        assertThat(request.id()).isEqualTo("test-id");
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo("/api/users");
        assertThat(request.queryParams()).isEqualTo("id=123");
        assertThat(request.headers()).isEqualTo(headers);
        assertThat(request.requestBody()).isEqualTo("{\"name\":\"John\"}");
        assertThat(request.responseStatus()).isEqualTo(200);
        assertThat(request.responseBody()).isEqualTo("{\"id\":\"123\",\"name\":\"John\"}");
        assertThat(request.timestamp()).isEqualTo(now);
    }

    @Test
    void shouldDetectJsonBodyWithObject() {
        // Given
        CapturedRequest request = new CapturedRequest(
                "id", "POST", "/api", null, Map.of(),
                "{\"key\":\"value\"}", 200, "", LocalDateTime.now()
        );

        // Then
        assertThat(request.hasJsonBody()).isTrue();
    }

    @Test
    void shouldDetectJsonBodyWithArray() {
        // Given
        CapturedRequest request = new CapturedRequest(
                "id", "GET", "/api", null, Map.of(),
                "[{\"key\":\"value\"}]", 200, "", LocalDateTime.now()
        );

        // Then
        assertThat(request.hasJsonBody()).isTrue();
    }

    @Test
    void shouldReturnFalseForNonJsonBody() {
        // Given
        CapturedRequest request = new CapturedRequest(
                "id", "POST", "/api", null, Map.of(),
                "plain text", 200, "", LocalDateTime.now()
        );

        // Then
        assertThat(request.hasJsonBody()).isFalse();
    }

    @Test
    void shouldReturnFalseForNullRequestBody() {
        // Given
        CapturedRequest request = new CapturedRequest(
                "id", "GET", "/api", null, Map.of(),
                null, 200, "", LocalDateTime.now()
        );

        // Then
        assertThat(request.hasJsonBody()).isFalse();
    }

    @Test
    void shouldReturnShortPathForShortPath() {
        // Given
        CapturedRequest request = new CapturedRequest(
                "id", "GET", "/api/users", null, Map.of(),
                null, 200, "", LocalDateTime.now()
        );

        // When
        String shortPath = request.getShortPath();

        // Then
        assertThat(shortPath).isEqualTo("/api/users");
    }

    @Test
    void shouldTruncateLongPath() {
        // Given
        String longPath = "/api/very/long/path/that/exceeds/thirty/characters";
        CapturedRequest request = new CapturedRequest(
                "id", "GET", longPath, null, Map.of(),
                null, 200, "", LocalDateTime.now()
        );

        // When
        String shortPath = request.getShortPath();

        // Then
        assertThat(shortPath).hasSize(30);
        assertThat(shortPath).endsWith("...");
        assertThat(shortPath).startsWith("/api/very/long/path/that/ex");
    }

    @Test
    void shouldReturnEmptyStringForNullPath() {
        // Given
        CapturedRequest request = new CapturedRequest(
                "id", "GET", null, null, Map.of(),
                null, 200, "", LocalDateTime.now()
        );

        // When
        String shortPath = request.getShortPath();

        // Then
        assertThat(shortPath).isEmpty();
    }

    @Test
    void shouldHandleExactly30CharactersPath() {
        // Given: exactly 30 chars
        String path = "/api/path123456789012345678901";
        CapturedRequest request = new CapturedRequest(
                "id", "GET", path, null, Map.of(),
                null, 200, "", LocalDateTime.now()
        );

        // When
        String shortPath = request.getShortPath();

        // Then
        assertThat(shortPath).isEqualTo(path);
        assertThat(shortPath).hasSize(30);
    }
}






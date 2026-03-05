package de.hofmannhbm.replay.core;

import de.hofmannhbm.replay.TestDataFactory;
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
        Map<String, String> requestHeaders = Map.of("Content-Type", "application/json");
        Map<String, String> responseHeaders = Map.of("X-Response", "test");

        // When
        CapturedRequest request = new CapturedRequest(
                "test-id",
                "POST",
                "/api/users",
                "id=123",
                requestHeaders,
                responseHeaders,
                "{\"name\":\"John\"}",
                200,
                "{\"id\":\"123\",\"name\":\"John\"}",
                "application/json",
                now
        );

        // Then
        assertThat(request.id()).isEqualTo("test-id");
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo("/api/users");
        assertThat(request.queryParams()).isEqualTo("id=123");
        assertThat(request.requestHeaders()).isEqualTo(requestHeaders);
        assertThat(request.responseHeaders()).isEqualTo(responseHeaders);
        assertThat(request.requestBody()).isEqualTo("{\"name\":\"John\"}");
        assertThat(request.responseStatus()).isEqualTo(200);
        assertThat(request.responseBody()).isEqualTo("{\"id\":\"123\",\"name\":\"John\"}");
        assertThat(request.responseContentType()).isEqualTo("application/json");
        assertThat(request.timestamp()).isEqualTo(now);
    }

    @Test
    void shouldDetectJsonBodyWithObject() {
        // Given
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("POST")
                .path("/api")
                .requestBody("{\"key\":\"value\"}")
                .build();

        // Then
        assertThat(request.hasJsonBody()).isTrue();
    }

    @Test
    void shouldDetectJsonBodyWithArray() {
        // Given
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("GET")
                .path("/api")
                .requestBody("[{\"key\":\"value\"}]")
                .build();

        // Then
        assertThat(request.hasJsonBody()).isTrue();
    }

    @Test
    void shouldReturnFalseForNonJsonBody() {
        // Given
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("POST")
                .path("/api")
                .requestBody("plain text")
                .build();

        // Then
        assertThat(request.hasJsonBody()).isFalse();
    }

    @Test
    void shouldReturnFalseForNullRequestBody() {
        // Given
        CapturedRequest request = TestDataFactory.createRequest("id", "GET", "/api");

        // Then
        assertThat(request.hasJsonBody()).isFalse();
    }

    @Test
    void shouldReturnShortPathForShortPath() {
        // Given
        CapturedRequest request = TestDataFactory.createRequest("id", "GET", "/api/users");

        // When
        String shortPath = request.getShortPath();

        // Then
        assertThat(shortPath).isEqualTo("/api/users");
    }

    @Test
    void shouldTruncateLongPath() {
        // Given
        String longPath = "/api/very/long/path/that/exceeds/thirty/characters";
        CapturedRequest request = TestDataFactory.createRequest("id", "GET", longPath);

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
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("GET")
                .path(null)
                .build();

        // When
        String shortPath = request.getShortPath();

        // Then
        assertThat(shortPath).isEmpty();
    }

    @Test
    void shouldHandleExactly30CharactersPath() {
        // Given: exactly 30 chars
        String path = "/api/path123456789012345678901";
        CapturedRequest request = TestDataFactory.createRequest("id", "GET", path);

        // When
        String shortPath = request.getShortPath();

        // Then
        assertThat(shortPath).isEqualTo(path);
        assertThat(shortPath).hasSize(30);
    }
}






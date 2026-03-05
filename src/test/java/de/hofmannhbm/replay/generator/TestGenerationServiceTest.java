package de.hofmannhbm.replay.generator;

import de.hofmannhbm.replay.TestDataFactory;
import de.hofmannhbm.replay.core.CapturedRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TestGenerationServiceTest {

    @Test
    void shouldGenerateTestsForAllRegisteredGenerators() {
        TestCodeGenerator mockMvcGen = new MockMvcGenerator();
        TestCodeGenerator customGen = new TestCodeGenerator() {
            @Override
            public String generate(CapturedRequest request) {
                return "custom test code";
            }

            @Override
            public String getName() {
                return "Custom";
            }
        };

        TestGenerationService service = new TestGenerationService(List.of(mockMvcGen, customGen));

        CapturedRequest request = TestDataFactory.createRequest("id", "GET", "/api/test");

        Map<String, String> result = service.generateAllFormats(request);

        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("MockMvc", "Custom");
        assertThat(result.get("Custom")).isEqualTo("custom test code");
    }

    @Test
    void shouldReturnEmptyMapWhenNoGeneratorsRegistered() {
        TestGenerationService service = new TestGenerationService(List.of());

        CapturedRequest request = TestDataFactory.createRequest("id", "GET", "/api/test");

        Map<String, String> result = service.generateAllFormats(request);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleSingleGenerator() {
        TestCodeGenerator mockMvcGen = new MockMvcGenerator();
        TestGenerationService service = new TestGenerationService(List.of(mockMvcGen));

        CapturedRequest request = TestDataFactory.createRequestWithBody(
                "id", "POST", "/api/users", "{\"name\":\"John\"}", 201, "{\"id\":1}"
        );

        Map<String, String> result = service.generateAllFormats(request);

        assertThat(result).hasSize(1);
        assertThat(result).containsKey("MockMvc");
        assertThat(result.get("MockMvc")).contains("post(\"/api/users\")");
    }

    @Test
    void shouldPassCorrectRequestToGenerators() {
        final CapturedRequest[] capturedInGenerator = {null};

        TestCodeGenerator trackingGen = new TestCodeGenerator() {
            @Override
            public String generate(CapturedRequest request) {
                capturedInGenerator[0] = request;
                return "test";
            }

            @Override
            public String getName() {
                return "Tracking";
            }
        };

        TestGenerationService service = new TestGenerationService(List.of(trackingGen));

        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("test-id")
                .method("DELETE")
                .path("/api/users/123")
                .queryString("force=true")
                .requestHeaders(Map.of("Auth", "token"))
                .statusCode(204)
                .build();

        service.generateAllFormats(request);

        assertThat(capturedInGenerator[0]).isEqualTo(request);
        assertThat(capturedInGenerator[0].id()).isEqualTo("test-id");
        assertThat(capturedInGenerator[0].method()).isEqualTo("DELETE");
        assertThat(capturedInGenerator[0].path()).isEqualTo("/api/users/123");
    }
}


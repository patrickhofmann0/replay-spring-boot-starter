package de.hofmannhbm.replay.generator;

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

        CapturedRequest request = new CapturedRequest(
                "id", "GET", "/api/test", null, Map.of(),
                null, 200, null, LocalDateTime.now()
        );

        Map<String, String> result = service.generateAllFormats(request);

        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("MockMvc", "Custom");
        assertThat(result.get("Custom")).isEqualTo("custom test code");
    }

    @Test
    void shouldReturnEmptyMapWhenNoGeneratorsRegistered() {
        TestGenerationService service = new TestGenerationService(List.of());

        CapturedRequest request = new CapturedRequest(
                "id", "GET", "/api/test", null, Map.of(),
                null, 200, null, LocalDateTime.now()
        );

        Map<String, String> result = service.generateAllFormats(request);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleSingleGenerator() {
        TestCodeGenerator mockMvcGen = new MockMvcGenerator();
        TestGenerationService service = new TestGenerationService(List.of(mockMvcGen));

        CapturedRequest request = new CapturedRequest(
                "id", "POST", "/api/users", null, Map.of(),
                "{\"name\":\"John\"}", 201, "{\"id\":1}", LocalDateTime.now()
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

        CapturedRequest request = new CapturedRequest(
                "test-id", "DELETE", "/api/users/123", "force=true", Map.of("Auth", "token"),
                null, 204, null, LocalDateTime.now()
        );

        service.generateAllFormats(request);

        assertThat(capturedInGenerator[0]).isEqualTo(request);
        assertThat(capturedInGenerator[0].id()).isEqualTo("test-id");
        assertThat(capturedInGenerator[0].method()).isEqualTo("DELETE");
        assertThat(capturedInGenerator[0].path()).isEqualTo("/api/users/123");
    }
}


package de.hofmannhbm.replay.generator;

import de.hofmannhbm.replay.core.CapturedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MockMvcGeneratorTest {

    private MockMvcGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new MockMvcGenerator();
    }

    @Test
    void shouldReturnCorrectName() {
        assertThat(generator.getName()).isEqualTo("MockMvc");
    }

    @Test
    void shouldGenerateSimpleGetRequest() {
        CapturedRequest request = new CapturedRequest(
                "id", "GET", "/api/users", null, Map.of(),
                null, 200, null, LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).contains("@Test");
        assertThat(code).contains("@DisplayName(\"Test for GET /api/users\")");
        assertThat(code).contains("mockMvc.perform(");
        assertThat(code).contains("get(\"/api/users\")");
        assertThat(code).contains(".andExpect(status().is(200))");
    }

    @Test
    void shouldGeneratePostRequest() {
        CapturedRequest request = new CapturedRequest(
                "id", "POST", "/api/users", null, Map.of(),
                null, 201, null, LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).contains("post(\"/api/users\")");
        assertThat(code).contains(".andExpect(status().is(201))");
    }

    @Test
    void shouldGeneratePutRequest() {
        CapturedRequest request = new CapturedRequest(
                "id", "PUT", "/api/users/1", null, Map.of(),
                null, 200, null, LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).contains("put(\"/api/users/1\")");
    }

    @Test
    void shouldGenerateDeleteRequest() {
        CapturedRequest request = new CapturedRequest(
                "id", "DELETE", "/api/users/1", null, Map.of(),
                null, 204, null, LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).contains("delete(\"/api/users/1\")");
        assertThat(code).contains(".andExpect(status().is(204))");
    }

    @Test
    void shouldIncludeRequestBodyWhenPresent() {
        String requestBody = "{\"name\":\"John\",\"email\":\"john@test.com\"}";
        CapturedRequest request = new CapturedRequest(
                "id", "POST", "/api/users", null, Map.of(),
                requestBody, 201, null, LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).contains(".content(\"\"\"");
        assertThat(code).contains(requestBody);
        assertThat(code).contains(".contentType(MediaType.APPLICATION_JSON)");
    }

    @Test
    void shouldIncludeResponseBodyAssertion() {
        String responseBody = "{\"id\":1,\"name\":\"John\"}";
        CapturedRequest request = new CapturedRequest(
                "id", "GET", "/api/users/1", null, Map.of(),
                null, 200, responseBody, LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).contains(".andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))");
        assertThat(code).contains(".andExpect(content().json(\"\"\"");
        assertThat(code).contains(responseBody);
    }

    @Test
    void shouldHandleQueryParameters() {
        CapturedRequest request = new CapturedRequest(
                "id", "GET", "/api/search", "q=test&page=1", Map.of(),
                null, 200, null, LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).contains(".queryParam(\"q\", \"test\")");
        assertThat(code).contains(".queryParam(\"page\", \"1\")");
    }

    @Test
    void shouldHandleMultipleQueryParameters() {
        CapturedRequest request = new CapturedRequest(
                "id", "GET", "/api/search", "q=test&page=1&limit=10", Map.of(),
                null, 200, null, LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).contains(".queryParam(\"q\", \"test\")");
        assertThat(code).contains(".queryParam(\"page\", \"1\")");
        assertThat(code).contains(".queryParam(\"limit\", \"10\")");
    }

    @Test
    void shouldHandleEmptyRequestBody() {
        CapturedRequest request = new CapturedRequest(
                "id", "POST", "/api/users", null, Map.of(),
                "", 201, null, LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).doesNotContain(".content(");
        assertThat(code).doesNotContain(".contentType(");
    }

    @Test
    void shouldHandleEmptyResponseBody() {
        CapturedRequest request = new CapturedRequest(
                "id", "DELETE", "/api/users/1", null, Map.of(),
                null, 204, "", LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).doesNotContain(".andExpect(content().json(");
    }

    @Test
    void shouldGenerateCompleteTestMethod() {
        CapturedRequest request = new CapturedRequest(
                "id", "POST", "/api/users", "source=test", Map.of(),
                "{\"name\":\"John\"}", 201, "{\"id\":1}", LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).contains("@Test");
        assertThat(code).contains("void shouldHandleRequest() throws Exception");
        assertThat(code).contains("mockMvc.perform(");
        assertThat(code).contains(";");
        assertThat(code).contains("}");
    }

    @Test
    void shouldHandle4xxStatusCodes() {
        CapturedRequest request = new CapturedRequest(
                "id", "GET", "/api/users/999", null, Map.of(),
                null, 404, "{\"error\":\"Not found\"}", LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).contains(".andExpect(status().is(404))");
        assertThat(code).contains("\"error\":\"Not found\"");
    }

    @Test
    void shouldHandle5xxStatusCodes() {
        CapturedRequest request = new CapturedRequest(
                "id", "POST", "/api/users", null, Map.of(),
                "{\"name\":\"John\"}", 500, "{\"error\":\"Internal error\"}", LocalDateTime.now()
        );

        String code = generator.generate(request);

        assertThat(code).contains(".andExpect(status().is(500))");
    }
}


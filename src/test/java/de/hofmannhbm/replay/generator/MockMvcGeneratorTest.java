package de.hofmannhbm.replay.generator;

import de.hofmannhbm.replay.TestDataFactory;
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
        CapturedRequest request = TestDataFactory.createRequest("id", "GET", "/api/users");

        String code = generator.generate(request);

        assertThat(code).contains("@Test");
        assertThat(code).contains("@DisplayName(\"Test for GET /api/users\")");
        assertThat(code).contains("mockMvc.perform(");
        assertThat(code).contains("get(\"/api/users\")");
        assertThat(code).contains(".andExpect(status().is(200))");
    }

    @Test
    void shouldGeneratePostRequest() {
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("POST")
                .path("/api/users")
                .statusCode(201)
                .build();

        String code = generator.generate(request);

        assertThat(code).contains("post(\"/api/users\")");
        assertThat(code).contains(".andExpect(status().is(201))");
    }

    @Test
    void shouldGeneratePutRequest() {
        CapturedRequest request = TestDataFactory.createRequest("id", "PUT", "/api/users/1");

        String code = generator.generate(request);

        assertThat(code).contains("put(\"/api/users/1\")");
    }

    @Test
    void shouldGenerateDeleteRequest() {
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("DELETE")
                .path("/api/users/1")
                .statusCode(204)
                .build();

        String code = generator.generate(request);

        assertThat(code).contains("delete(\"/api/users/1\")");
        assertThat(code).contains(".andExpect(status().is(204))");
    }

    @Test
    void shouldIncludeRequestBodyWhenPresent() {
        String requestBody = "{\"name\":\"John\",\"email\":\"john@test.com\"}";
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("POST")
                .path("/api/users")
                .requestBody(requestBody)
                .statusCode(201)
                .build();

        String code = generator.generate(request);

        assertThat(code).contains(".content(\"\"\"");
        assertThat(code).contains(requestBody);
        assertThat(code).contains(".contentType(MediaType.APPLICATION_JSON)");
    }

    @Test
    void shouldIncludeResponseBodyAssertion() {
        String responseBody = "{\"id\":1,\"name\":\"John\"}";
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("GET")
                .path("/api/users/1")
                .responseBody(responseBody)
                .responseContentType("application/json")
                .build();

        String code = generator.generate(request);

        assertThat(code).contains(".andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))");
        assertThat(code).contains(".andExpect(content().json(\"\"\"");
        assertThat(code).contains(responseBody);
    }

    @Test
    void shouldHandleQueryParameters() {
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("GET")
                .path("/api/search")
                .queryString("q=test&page=1")
                .build();

        String code = generator.generate(request);

        assertThat(code).contains(".queryParam(\"q\", \"test\")");
        assertThat(code).contains(".queryParam(\"page\", \"1\")");
    }

    @Test
    void shouldHandleMultipleQueryParameters() {
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("GET")
                .path("/api/search")
                .queryString("q=test&page=1&limit=10")
                .build();

        String code = generator.generate(request);

        assertThat(code).contains(".queryParam(\"q\", \"test\")");
        assertThat(code).contains(".queryParam(\"page\", \"1\")");
        assertThat(code).contains(".queryParam(\"limit\", \"10\")");
    }

    @Test
    void shouldHandleEmptyRequestBody() {
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("POST")
                .path("/api/users")
                .requestBody("")
                .statusCode(201)
                .build();

        String code = generator.generate(request);

        assertThat(code).doesNotContain(".content(");
        assertThat(code).doesNotContain(".contentType(");
    }

    @Test
    void shouldHandleEmptyResponseBody() {
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("DELETE")
                .path("/api/users/1")
                .statusCode(204)
                .responseBody("")
                .build();

        String code = generator.generate(request);

        assertThat(code).doesNotContain(".andExpect(content().json(");
    }

    @Test
    void shouldGenerateCompleteTestMethod() {
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("POST")
                .path("/api/users")
                .queryString("source=test")
                .requestBody("{\"name\":\"John\"}")
                .statusCode(201)
                .responseBody("{\"id\":1}")
                .responseContentType("application/json")
                .build();

        String code = generator.generate(request);

        assertThat(code).contains("@Test");
        assertThat(code).contains("void shouldHandleRequest() throws Exception");
        assertThat(code).contains("mockMvc.perform(");
        assertThat(code).contains(";");
        assertThat(code).contains("}");
    }

    @Test
    void shouldHandle4xxStatusCodes() {
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("GET")
                .path("/api/users/999")
                .statusCode(404)
                .responseBody("{\"error\":\"Not found\"}")
                .responseContentType("application/json")
                .build();

        String code = generator.generate(request);

        assertThat(code).contains(".andExpect(status().is(404))");
        assertThat(code).contains("\"error\":\"Not found\"");
    }

    @Test
    void shouldHandle5xxStatusCodes() {
        CapturedRequest request = TestDataFactory.createRequestBuilder()
                .id("id")
                .method("POST")
                .path("/api/users")
                .requestBody("{\"name\":\"John\"}")
                .statusCode(500)
                .responseBody("{\"error\":\"Internal error\"}")
                .responseContentType("application/json")
                .build();

        String code = generator.generate(request);

        assertThat(code).contains(".andExpect(status().is(500))");
    }
}


package io.github.patrickhofmann0.replay.core;

import io.github.patrickhofmann0.replay.config.ReplayProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReplayCaptureFilterTest {

    private InMemoryReplayRequestStorage repository;
    private ReplayCaptureFilter filter;

    @BeforeEach
    void setUp() {
        repository = new InMemoryReplayRequestStorage(100);
        ReplayProperties properties = new ReplayProperties(true, true, 100, null, null);
        filter = new ReplayCaptureFilter(repository, properties);
    }

    @Test
    void shouldCaptureSimpleGetRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(200);
            res.getWriter().write("Success");
        };

        filter.doFilter(request, response, chain);

        List<CapturedRequest> captured = repository.findAll();
        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).method()).isEqualTo("GET");
        assertThat(captured.get(0).path()).isEqualTo("/api/users");
        assertThat(captured.get(0).responseStatus()).isEqualTo(200);
    }

    @Test
    void shouldCapturePostRequestWithBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");
        String requestBody = "{\"name\":\"John\"}";
        request.setContent(requestBody.getBytes());
        request.setContentType("application/json");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            // Read the request body to trigger caching
            try {
                req.getInputStream().readAllBytes();
            } catch (Exception e) {
                // ignore
            }
            ((HttpServletResponse) res).setStatus(201);
            res.getWriter().write("{\"id\":1,\"name\":\"John\"}");
        };

        filter.doFilter(request, response, chain);

        List<CapturedRequest> captured = repository.findAll();
        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).requestBody()).contains("John");
        assertThat(captured.get(0).responseStatus()).isEqualTo(201);
    }

    @Test
    void shouldExcludeReplayDashboardPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/replay/dashboard");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(200);

        filter.doFilter(request, response, chain);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldExcludeActuatorPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(200);

        filter.doFilter(request, response, chain);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldRedactAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/secured");
        request.addHeader("Authorization", "Bearer secret-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(200);

        filter.doFilter(request, response, chain);

        List<CapturedRequest> captured = repository.findAll();
        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).headers().get("Authorization")).isEqualTo("[REDACTED]");
    }

    @Test
    void shouldRedactCookieHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/data");
        request.addHeader("Cookie", "session=abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(200);

        filter.doFilter(request, response, chain);

        List<CapturedRequest> captured = repository.findAll();
        assertThat(captured.get(0).headers().get("Cookie")).isEqualTo("[REDACTED]");
    }

    @Test
    void shouldNotRedactNonSensitiveHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/data");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(200);

        filter.doFilter(request, response, chain);

        List<CapturedRequest> captured = repository.findAll();
        assertThat(captured.get(0).headers().get("Content-Type")).isEqualTo("application/json");
        assertThat(captured.get(0).headers().get("Accept")).isEqualTo("application/json");
    }

    @Test
    void shouldExcludeCustomPaths() throws Exception {
        ReplayProperties properties = new ReplayProperties(
                true, true, 100, List.of("/custom/**"), null
        );
        filter = new ReplayCaptureFilter(repository, properties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/custom/excluded");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(200);

        filter.doFilter(request, response, chain);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldCaptureQueryParameters() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/search");
        request.setQueryString("q=test&page=1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(200);

        filter.doFilter(request, response, chain);

        List<CapturedRequest> captured = repository.findAll();
        assertThat(captured.get(0).queryParams()).isEqualTo("q=test&page=1");
    }
}



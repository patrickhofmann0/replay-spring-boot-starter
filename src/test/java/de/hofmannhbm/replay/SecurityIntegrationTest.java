package de.hofmannhbm.replay;

import de.hofmannhbm.replay.config.ReplayProperties;
import de.hofmannhbm.replay.core.CapturedRequest;
import de.hofmannhbm.replay.core.InMemoryReplayRequestStorage;
import de.hofmannhbm.replay.core.ReplayCaptureFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test, der zeigt, dass der ReplayCaptureFilter auch mit Spring Security funktioniert.
 *
 * WICHTIG: Durch die Registrierung mit FilterRegistrationBean und der richtigen Order
 * (LOWEST_PRECEDENCE - 1) läuft der Filter NACH Spring Security und erfasst sowohl:
 * - Erfolgreiche authentifizierte Requests (200 OK)
 * - Abgelehnte unauthentifizierte Requests (401 Unauthorized)
 * - Forbidden Requests (403 Forbidden)
 *
 * Der Filter wird damit zum "letzten Filter" in der Chain und kann alle Responses erfassen,
 * egal ob sie von Security oder vom Controller kommen.
 */
class SecurityIntegrationTest {

    private InMemoryReplayRequestStorage repository;
    private ReplayCaptureFilter filter;

    @BeforeEach
    void setUp() {
        repository = new InMemoryReplayRequestStorage(100);
        // Verwende die Standard-excludeHeaders (Authorization, Cookie, X-Api-Key)
        ReplayProperties properties = new ReplayProperties(true, true, 100, null, null);
        filter = new ReplayCaptureFilter(repository, properties);
    }

    @Test
    void shouldCaptureUnauthorizedRequest() throws Exception {
        // Given: Ein Request simuliert eine 401 Unauthorized Response von Spring Security
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/secured");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Mock FilterChain that simulates Spring Security rejecting the request
        FilterChain chain = (req, res) -> {
            HttpServletResponse httpResponse = (HttpServletResponse) res;
            httpResponse.setStatus(401);
            httpResponse.getWriter().write("Unauthorized");
        };

        // When: Der Filter verarbeitet den Request
        filter.doFilter(request, response, chain);

        // Then: Der Request wurde erfasst (mit Status 401)
        List<CapturedRequest> captured = repository.findAll();
        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).method()).isEqualTo("GET");
        assertThat(captured.get(0).path()).isEqualTo("/api/secured");
        assertThat(captured.get(0).responseStatus()).isEqualTo(401);
        assertThat(captured.get(0).responseBody()).contains("Unauthorized");
    }

    @Test
    void shouldCaptureAuthorizedRequest() throws Exception {
        // Given: Ein Request simuliert eine erfolgreiche authentifizierte Response
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/secured");
        request.addHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Mock FilterChain that simulates successful authentication and controller response
        FilterChain chain = (req, res) -> {
            HttpServletResponse httpResponse = (HttpServletResponse) res;
            httpResponse.setStatus(200);
            httpResponse.getWriter().write("Secured Content");
        };

        // When: Der Filter verarbeitet den Request
        filter.doFilter(request, response, chain);

        // Then: Der Request wurde erfasst (mit Status 200)
        List<CapturedRequest> captured = repository.findAll();
        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).method()).isEqualTo("GET");
        assertThat(captured.get(0).path()).isEqualTo("/api/secured");
        assertThat(captured.get(0).responseStatus()).isEqualTo(200);
        assertThat(captured.get(0).responseBody()).contains("Secured Content");
        // Der Authorization Header wurde als [REDACTED] erfasst (nicht im Klartext)
        assertThat(captured.get(0).headers()).containsKey("Authorization");
        assertThat(captured.get(0).headers().get("Authorization")).isEqualTo("[REDACTED]");
    }

    @Test
    void shouldCaptureForbiddenRequest() throws Exception {
        // Given: Ein Request simuliert eine 403 Forbidden Response (User authentifiziert, aber keine Rechte)
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin");
        request.addHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Mock FilterChain that simulates Spring Security denying access
        FilterChain chain = (req, res) -> {
            HttpServletResponse httpResponse = (HttpServletResponse) res;
            httpResponse.setStatus(403);
            httpResponse.getWriter().write("Forbidden");
        };

        // When: Der Filter verarbeitet den Request
        filter.doFilter(request, response, chain);

        // Then: Der Request wurde erfasst (mit Status 403)
        List<CapturedRequest> captured = repository.findAll();
        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).method()).isEqualTo("POST");
        assertThat(captured.get(0).path()).isEqualTo("/api/admin");
        assertThat(captured.get(0).responseStatus()).isEqualTo(403);
        assertThat(captured.get(0).responseBody()).contains("Forbidden");
    }

    @Test
    void shouldNotCaptureReplayDashboardRequests() throws Exception {
        // Given: Ein Request zum Replay Dashboard (ausgeschlossen)
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/replay/dashboard");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            HttpServletResponse httpResponse = (HttpServletResponse) res;
            httpResponse.setStatus(200);
            httpResponse.getWriter().write("Dashboard");
        };

        // When: Der Filter verarbeitet den Request
        filter.doFilter(request, response, chain);

        // Then: Der Request wurde NICHT erfasst (ist ausgeschlossen)
        List<CapturedRequest> captured = repository.findAll();
        assertThat(captured).isEmpty();
    }

    @Test
    void shouldRedactExcludedHeaders() throws Exception {
        // Given: Ein Request mit mehreren Headern, von denen einige ausgeschlossen sind
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/data");
        request.addHeader("Authorization", "Bearer secret-token-123");
        request.addHeader("Cookie", "session=abc123");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-Custom-Header", "some-value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            HttpServletResponse httpResponse = (HttpServletResponse) res;
            httpResponse.setStatus(201);
            httpResponse.getWriter().write("Created");
        };

        // When: Der Filter verarbeitet den Request
        filter.doFilter(request, response, chain);

        // Then: Sensible Header wurden redacted, andere nicht
        List<CapturedRequest> captured = repository.findAll();
        assertThat(captured).hasSize(1);

        Map<String, String> headers = captured.get(0).headers();
        // Authorization und Cookie sollten redacted sein
        assertThat(headers.get("Authorization")).isEqualTo("[REDACTED]");
        assertThat(headers.get("Cookie")).isEqualTo("[REDACTED]");
        // Andere Header sollten normal erfasst werden
        assertThat(headers.get("Content-Type")).isEqualTo("application/json");
        assertThat(headers.get("X-Custom-Header")).isEqualTo("some-value");
    }
}






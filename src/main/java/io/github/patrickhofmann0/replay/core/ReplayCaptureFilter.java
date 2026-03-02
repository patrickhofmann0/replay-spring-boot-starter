package io.github.patrickhofmann0.replay.core;


import io.github.patrickhofmann0.replay.config.ReplayProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

public class ReplayCaptureFilter extends OncePerRequestFilter {

    private static final int MAX_CONTENT_CACHE_SIZE = 10 * 1024; // 10 KB

    private final ReplayRequestRepository repository;
    private final ReplayProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ReplayCaptureFilter(ReplayRequestRepository repository, ReplayProperties replayProperties) {
        this.repository = repository;
        this.properties = replayProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Requests auf ausgeschlossenen Pfaden nicht wrappen
        if (shouldExclude(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Requests und Responses "einpacken" um sie cachebar zu machen
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, MAX_CONTENT_CACHE_SIZE);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            // 2. Den Request weiterreichen an den Controller
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 3. Erst im "finally" sind die Caches gefüllt. Jetzt extrahieren wir die Daten.
            capture(requestWrapper, responseWrapper);

            // 4. ESSENZIELL: Den Response-Body zurück in den echten Output-Stream kopieren,
            // sonst erhält der Client eine leere Antwort!
            responseWrapper.copyBodyToResponse();
        }
    }

    private boolean shouldExclude(String path) {
        var defaultPatchMatch = Stream.of("/replay/**", "/actuator/**")
                .anyMatch(exclude -> pathMatcher.match(exclude, path));
        if (defaultPatchMatch) {
            return true;
        }
        if (properties.excludePaths() == null) {
            return false;
        }
        return properties.excludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                // Header ausschließen, wenn er in der Exclude-Liste ist (case-insensitive)
                if (shouldExcludeHeader(name)) {
                    headers.put(name, "[REDACTED]");
                } else {
                    headers.put(name, request.getHeader(name));
                }
            }
        }
        return Collections.unmodifiableMap(headers);
    }

    private boolean shouldExcludeHeader(String headerName) {
        if (properties.excludeHeaders() == null || properties.excludeHeaders().isEmpty()) {
            return false;
        }
        return properties.excludeHeaders().stream()
                .anyMatch(excluded -> excluded.equalsIgnoreCase(headerName));
    }

    private void capture(ContentCachingRequestWrapper req, ContentCachingResponseWrapper res) {
        try {
            // Wir lesen die Bytes aus dem Cache
            String requestBody = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseBody = new String(res.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseContentType = res.getContentType();

            CapturedRequest captured = new CapturedRequest(
                    UUID.randomUUID().toString(),
                    req.getMethod(),
                    req.getRequestURI(),
                    req.getQueryString(),
                    extractHeaders(req),
                    requestBody,
                    res.getStatus(),
                    responseBody,
                    LocalDateTime.now()
            );

            // In den In-Memory Buffer speichern
            repository.save(captured);
        } catch (Exception e) {
            // Log error but don't fail the request
            if (logger.isErrorEnabled()) {
                logger.error("Failed to capture request", e);
            }
        }
    }

}
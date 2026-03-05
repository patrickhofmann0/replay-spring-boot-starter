package de.hofmannhbm.replay;

import de.hofmannhbm.replay.core.CapturedRequest;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Factory für Test-Daten um CapturedRequest-Instanzen zu erstellen.
 */
public class TestDataFactory {

    public static CapturedRequest createRequest(String id, String method, String path) {
        return new CapturedRequest(
                id,
                method,
                path,
                null,
                Map.of(),
                Map.of(),
                "",
                200,
                "",
                null,
                LocalDateTime.now()
        );
    }

    public static CapturedRequest createRequestWithBody(String id, String method, String path, 
                                                        String requestBody, String responseBody) {
        return new CapturedRequest(
                id,
                method,
                path,
                null,
                Map.of(),
                Map.of(),
                requestBody,
                200,
                responseBody,
                "application/json",
                LocalDateTime.now()
        );
    }

    public static CapturedRequest createRequestWithHeaders(String id, String method, String path,
                                                           Map<String, String> requestHeaders,
                                                           Map<String, String> responseHeaders) {
        return new CapturedRequest(
                id,
                method,
                path,
                null,
                requestHeaders,
                responseHeaders,
                "",
                200,
                "",
                null,
                LocalDateTime.now()
        );
    }

    public static CapturedRequest createFullRequest(String id, String method, String path,
                                                    String queryParams,
                                                    Map<String, String> requestHeaders,
                                                    Map<String, String> responseHeaders,
                                                    String requestBody,
                                                    int responseStatus,
                                                    String responseBody,
                                                    String responseContentType) {
        return new CapturedRequest(
                id,
                method,
                path,
                queryParams,
                requestHeaders,
                responseHeaders,
                requestBody,
                responseStatus,
                responseBody,
                responseContentType,
                LocalDateTime.now()
        );
    }
}

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

    public static CapturedRequest createRequestWithBody(String id, String method, String path,
                                                        String requestBody, int statusCode, String responseBody) {
        return new CapturedRequest(
                id,
                method,
                path,
                null,
                Map.of(),
                Map.of(),
                requestBody,
                statusCode,
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

    public static Builder createRequestBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String id = "test-id";
        private String method = "GET";
        private String path = "/test";
        private String queryString = null;
        private Map<String, String> requestHeaders = Map.of();
        private Map<String, String> responseHeaders = Map.of();
        private String requestBody = null;
        private int statusCode = 200;
        private String responseBody = null;
        private String responseContentType = null;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder requestHeaders(Map<String, String> requestHeaders) {
            this.requestHeaders = requestHeaders;
            return this;
        }

        public Builder responseHeaders(Map<String, String> responseHeaders) {
            this.responseHeaders = responseHeaders;
            return this;
        }

        public Builder requestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder responseBody(String responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Builder responseContentType(String responseContentType) {
            this.responseContentType = responseContentType;
            return this;
        }

        public CapturedRequest build() {
            return new CapturedRequest(
                    id,
                    method,
                    path,
                    queryString,
                    requestHeaders,
                    responseHeaders,
                    requestBody,
                    statusCode,
                    responseBody,
                    responseContentType,
                    LocalDateTime.now()
            );
        }
    }
}

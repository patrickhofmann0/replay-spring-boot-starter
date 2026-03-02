package io.github.patrickhofmann0.replay.generator;

import io.github.patrickhofmann0.replay.core.CapturedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockMvcGenerator implements TestCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(MockMvcGenerator.class);

    @Override
    public String generate(CapturedRequest request) {
        StringBuilder test = new StringBuilder();

        test.append("""
                @Test
                @DisplayName("Test for %s %s")
                void shouldHandleRequest() throws Exception {
                    mockMvc.perform(
                        %s("%s")
                """.formatted(
                request.method(),
                request.path(),
                request.method().toLowerCase(),
                request.path()
        ));

        // Add query parameters if present
        if (request.queryParams() != null && !request.queryParams().isBlank()) {
            String[] params = request.queryParams().split("&");
            for (String param : params) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2) {
                    test.append("""
                            .queryParam("%s", "%s")
                    """.formatted(keyValue[0], keyValue[1]));
                }
            }
        }

        // Add request body if present
        if (request.requestBody() != null && !request.requestBody().isBlank()) {
            test.append("""
                            .content(\"""
                                %s
                                \""")
                            .contentType(MediaType.APPLICATION_JSON)
                    """.formatted(request.requestBody()));
        }

        test.append("""
                    )
                    .andExpect(status().is(%d))
                """.formatted(request.responseStatus()));

        // Add response body assertions if present
        if (request.responseBody() != null && !request.responseBody().isBlank()) {
            test.append("""
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(content().json(\"""
                            %s
                            \""", true))
                    """.formatted(request.responseBody()));
        }

        test.append("""
                    ;
                }
                """);

        return test.toString();
    }

    @Override
    public String getName() {
        return "MockMvc";
    }
}
package io.github.patrickhofmann0.replay.generator;

import io.github.patrickhofmann0.replay.core.CapturedRequest;

public interface TestCodeGenerator {
    String generate(CapturedRequest request);
    String getName(); // z.B. "MockMvc", "RestAssured"
}
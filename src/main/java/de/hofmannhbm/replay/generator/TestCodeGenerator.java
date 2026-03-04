package de.hofmannhbm.replay.generator;

import de.hofmannhbm.replay.core.CapturedRequest;

public interface TestCodeGenerator {
    String generate(CapturedRequest request);
    String getName(); // z.B. "MockMvc", "RestAssured"
}
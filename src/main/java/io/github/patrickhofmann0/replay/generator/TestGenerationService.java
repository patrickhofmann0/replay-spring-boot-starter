package io.github.patrickhofmann0.replay.generator;

import io.github.patrickhofmann0.replay.core.CapturedRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestGenerationService {

    private final List<TestCodeGenerator> generators;

    public TestGenerationService(List<TestCodeGenerator> generators) {
        // Filtere nur die Generatoren, deren Frameworks auch wirklich da sind
        this.generators = generators.stream()
                                    .toList();
    }

    public Map<String, String> generateAllFormats(CapturedRequest request) {
        return generators.stream()
            .collect(Collectors.toMap(
                TestCodeGenerator::getName,
                g -> g.generate(request)
            ));
    }
}
package io.github.patrickhofmann0.replay.config;

import io.github.patrickhofmann0.replay.core.InMemoryReplayRequestStorage;
import io.github.patrickhofmann0.replay.core.ReplayCaptureFilter;
import io.github.patrickhofmann0.replay.core.ReplayRequestRepository;
import io.github.patrickhofmann0.replay.generator.MockMvcGenerator;
import io.github.patrickhofmann0.replay.generator.TestCodeGenerator;
import io.github.patrickhofmann0.replay.generator.TestGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReplayAutoConfigurationTest {

    @Test
    void shouldCreateInMemoryRepositoryWithCorrectMaxSize() {
        ReplayProperties properties = new ReplayProperties(true, true, 50, null, null);
        ReplayAutoConfiguration config = new ReplayAutoConfiguration();

        ReplayRequestRepository repository = config.inMemoryRequestRepository(properties);

        assertThat(repository).isInstanceOf(InMemoryReplayRequestStorage.class);
        InMemoryReplayRequestStorage storage = (InMemoryReplayRequestStorage) repository;

        // Verify max size by filling it
        for (int i = 0; i < 60; i++) {
            storage.save(new io.github.patrickhofmann0.replay.core.CapturedRequest(
                    "id-" + i, "GET", "/test", null, java.util.Map.of(),
                    null, 200, null, java.time.LocalDateTime.now()
            ));
        }
        assertThat(storage.findAll()).hasSize(50);
    }

    @Test
    void shouldCreateReplayCaptureFilter() {
        ReplayProperties properties = new ReplayProperties(true, true, 100, null, null);
        InMemoryReplayRequestStorage storage = new InMemoryReplayRequestStorage(100);
        ReplayAutoConfiguration config = new ReplayAutoConfiguration();

        ReplayCaptureFilter filter = config.replayCaptureFilter(storage, properties);

        assertThat(filter).isNotNull();
    }

    @Test
    void shouldRegisterFilterWithCorrectOrder() {
        ReplayProperties properties = new ReplayProperties(true, true, 100, null, null);
        InMemoryReplayRequestStorage storage = new InMemoryReplayRequestStorage(100);
        ReplayAutoConfiguration config = new ReplayAutoConfiguration();
        ReplayCaptureFilter filter = config.replayCaptureFilter(storage, properties);

        FilterRegistrationBean<ReplayCaptureFilter> registration =
                config.replayCaptureFilterRegistration(filter);

        assertThat(registration.getFilter()).isEqualTo(filter);
        assertThat(registration.getOrder()).isEqualTo(Integer.MAX_VALUE - 1);
        assertThat(registration.getUrlPatterns()).contains("/*");
    }

    @Test
    void shouldCreateMockMvcGenerator() {
        ReplayAutoConfiguration config = new ReplayAutoConfiguration();

        TestCodeGenerator generator = config.mockMvcGenerator();

        assertThat(generator).isInstanceOf(MockMvcGenerator.class);
        assertThat(generator.getName()).isEqualTo("MockMvc");
    }

    @Test
    void shouldCreateTestGenerationService() {
        ReplayAutoConfiguration config = new ReplayAutoConfiguration();
        TestCodeGenerator mockMvcGen = config.mockMvcGenerator();

        TestGenerationService service = config.testGenerationService(List.of(mockMvcGen));

        assertThat(service).isNotNull();

        // Verify it works with a simple request
        io.github.patrickhofmann0.replay.core.CapturedRequest request =
                new io.github.patrickhofmann0.replay.core.CapturedRequest(
                        "id", "GET", "/test", null, java.util.Map.of(),
                        null, 200, null, java.time.LocalDateTime.now()
                );

        java.util.Map<String, String> result = service.generateAllFormats(request);
        assertThat(result).containsKey("MockMvc");
    }

    @Test
    void shouldCreateTestGenerationServiceWithMultipleGenerators() {
        ReplayAutoConfiguration config = new ReplayAutoConfiguration();
        TestCodeGenerator mockMvcGen = config.mockMvcGenerator();
        TestCodeGenerator customGen = new TestCodeGenerator() {
            @Override
            public String generate(io.github.patrickhofmann0.replay.core.CapturedRequest request) {
                return "custom";
            }

            @Override
            public String getName() {
                return "Custom";
            }
        };

        TestGenerationService service = config.testGenerationService(
                List.of(mockMvcGen, customGen)
        );

        io.github.patrickhofmann0.replay.core.CapturedRequest request =
                new io.github.patrickhofmann0.replay.core.CapturedRequest(
                        "id", "GET", "/test", null, java.util.Map.of(),
                        null, 200, null, java.time.LocalDateTime.now()
                );

        java.util.Map<String, String> result = service.generateAllFormats(request);
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("MockMvc", "Custom");
    }
}


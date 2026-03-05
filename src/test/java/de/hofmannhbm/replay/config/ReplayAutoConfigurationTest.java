package de.hofmannhbm.replay.config;

import de.hofmannhbm.replay.TestDataFactory;
import de.hofmannhbm.replay.core.CapturedRequest;
import de.hofmannhbm.replay.core.InMemoryReplayRequestStorage;
import de.hofmannhbm.replay.core.ReplayCaptureFilter;
import de.hofmannhbm.replay.core.ReplayRequestRepository;
import de.hofmannhbm.replay.generator.MockMvcGenerator;
import de.hofmannhbm.replay.generator.TestCodeGenerator;
import de.hofmannhbm.replay.generator.TestGenerationService;
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
            storage.save(TestDataFactory.createRequest("id-" + i, "GET", "/test"));
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
        CapturedRequest request = TestDataFactory.createRequest("id", "GET", "/test");

        java.util.Map<String, String> result = service.generateAllFormats(request);
        assertThat(result).containsKey("MockMvc");
    }

    @Test
    void shouldCreateTestGenerationServiceWithMultipleGenerators() {
        ReplayAutoConfiguration config = new ReplayAutoConfiguration();
        TestCodeGenerator mockMvcGen = config.mockMvcGenerator();
        TestCodeGenerator customGen = new TestCodeGenerator() {
            @Override
            public String generate(CapturedRequest request) {
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

        CapturedRequest request = TestDataFactory.createRequest("id", "GET", "/test");

        java.util.Map<String, String> result = service.generateAllFormats(request);
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("MockMvc", "Custom");
    }
}


package io.github.patrickhofmann0.replay.config;

import io.github.patrickhofmann0.replay.ReplayDashboardController;
import io.github.patrickhofmann0.replay.core.InMemoryReplayRequestStorage;
import io.github.patrickhofmann0.replay.core.ReplayCaptureFilter;
import io.github.patrickhofmann0.replay.core.ReplayRequestRepository;
import io.github.patrickhofmann0.replay.generator.MockMvcGenerator;
import io.github.patrickhofmann0.replay.generator.TestCodeGenerator;
import io.github.patrickhofmann0.replay.generator.TestGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(ReplayProperties.class)
@ConditionalOnProperty(prefix = "replay", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(ReplayDashboardController.class)
public class ReplayAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ReplayAutoConfiguration.class);

    public ReplayAutoConfiguration() {
        log.info("Replay auto configuration initialized");
    }

    @Bean
    @ConditionalOnMissingBean
    public ReplayRequestRepository inMemoryRequestRepository(ReplayProperties properties) {
        log.info("Provide InMemoryRequestRepository");
        return new InMemoryReplayRequestStorage(properties.maxCapturedRequests());
    }

    @Bean
    public ReplayCaptureFilter replayCaptureFilter(ReplayRequestRepository repository,
                                                   ReplayProperties properties) {
        log.info("Provide ReplayCaptureFilter");
        return new ReplayCaptureFilter(repository, properties);
    }

    @Bean
    public FilterRegistrationBean<ReplayCaptureFilter> replayCaptureFilterRegistration(
            ReplayCaptureFilter replayCaptureFilter) {
        FilterRegistrationBean<ReplayCaptureFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(replayCaptureFilter);
        // WICHTIG: Order NACH Spring Security (Security Filter hat Order = -100)
        // Damit erfassen wir auch Requests, die von Security abgelehnt werden
        registration.setOrder(Ordered.LOWEST_PRECEDENCE - 1);
        registration.addUrlPatterns("/*");
        log.info("Registered ReplayCaptureFilter with order: {}", registration.getOrder());
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public TestCodeGenerator mockMvcGenerator() {
        log.info("Provide MockMvcGenerator");
        return new MockMvcGenerator();
    }

    @Bean
    public TestGenerationService testGenerationService(List<TestCodeGenerator> generators) {
        log.info("Provide TestGenerationService with {} generators", generators.size());
        return new TestGenerationService(generators);
    }
}
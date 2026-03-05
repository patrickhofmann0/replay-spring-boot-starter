package de.hofmannhbm.replay;

import de.hofmannhbm.replay.TestDataFactory;
import de.hofmannhbm.replay.core.CapturedRequest;
import de.hofmannhbm.replay.core.InMemoryReplayRequestStorage;
import de.hofmannhbm.replay.generator.TestGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplayDashboardControllerTest {

    @Mock
    private InMemoryReplayRequestStorage repository;

    @Mock
    private TestGenerationService generationService;

    private ReplayDashboardController controller;

    @BeforeEach
    void setUp() {
        controller = new ReplayDashboardController(repository, generationService);
    }

    @Test
    void shouldReturnDashboardViewWithRequests() {
        // Given
        List<CapturedRequest> requests = List.of(
                TestDataFactory.createRequest("id-1", "GET", "/api/users"),
                TestDataFactory.createRequest("id-2", "POST", "/api/users")
        );
        when(repository.findAll()).thenReturn(requests);
        Model model = new ConcurrentModel();

        // When
        String viewName = controller.dashboard(model);

        // Then
        assertThat(viewName).isEqualTo("replay/dashboard");
        assertThat(model.getAttribute("requests")).isEqualTo(requests);
    }

    @Test
    void shouldReturnRequestsTableFragment() {
        // Given
        List<CapturedRequest> requests = List.of(
                TestDataFactory.createRequest("id-1", "GET", "/api/test")
        );
        when(repository.findAll()).thenReturn(requests);
        Model model = new ConcurrentModel();

        // When
        String viewName = controller.getRequestsTable(model);

        // Then
        assertThat(viewName).isEqualTo("replay/dashboard :: request-table-rows");
        assertThat(model.getAttribute("requests")).isEqualTo(requests);
    }

    @Test
    void shouldReturnRequestDetailsWhenFound() {
        // Given
        CapturedRequest request = TestDataFactory.createRequest("test-id", "POST", "/api/users");
        when(repository.findById("test-id")).thenReturn(request);
        Model model = new ConcurrentModel();

        // When
        String viewName = controller.getRequestDetails("test-id", model);

        // Then
        assertThat(viewName).isEqualTo("replay/request-details :: details");
        assertThat(model.getAttribute("request")).isEqualTo(request);
        assertThat(model.getAttribute("error")).isNull();
    }

    @Test
    void shouldReturnErrorWhenRequestNotFound() {
        // Given
        when(repository.findById("non-existent")).thenReturn(null);
        Model model = new ConcurrentModel();

        // When
        String viewName = controller.getRequestDetails("non-existent", model);

        // Then
        assertThat(viewName).isEqualTo("replay/request-details :: error");
        assertThat(model.getAttribute("error")).isEqualTo(true);
        assertThat(model.getAttribute("errorMessage"))
                .asString()
                .contains("non-existent")
                .contains("konnte nicht gefunden werden");
    }

    @Test
    void shouldGenerateTestSnippetsWhenRequestFound() {
        // Given
        CapturedRequest request = TestDataFactory.createRequest("test-id", "GET", "/api/test");
        Map<String, String> snippets = Map.of(
                "MockMvc", "test code here",
                "RestAssured", "rest assured code"
        );
        when(repository.findById("test-id")).thenReturn(request);
        when(generationService.generateAllFormats(request)).thenReturn(snippets);
        Model model = new ConcurrentModel();

        // When
        String viewName = controller.generateSnippet("test-id", model);

        // Then
        assertThat(viewName).isEqualTo("replay/test-snippets :: snippets");
        assertThat(model.getAttribute("snippets")).isEqualTo(snippets.entrySet());
    }

    @Test
    void shouldReturnErrorWhenGeneratingForNonExistentRequest() {
        // Given
        when(repository.findById("non-existent")).thenReturn(null);
        Model model = new ConcurrentModel();

        // When
        String viewName = controller.generateSnippet("non-existent", model);

        // Then
        assertThat(viewName).isEqualTo("replay/test-snippets :: error");
        assertThat(model.getAttribute("errorMessage"))
                .asString()
                .contains("non-existent")
                .contains("konnte nicht gefunden werden");
    }

    @Test
    void shouldReturnWarningWhenNoSnippetsGenerated() {
        // Given
        CapturedRequest request = TestDataFactory.createRequest("test-id", "GET", "/api/test");
        when(repository.findById("test-id")).thenReturn(request);
        when(generationService.generateAllFormats(request)).thenReturn(Map.of());
        Model model = new ConcurrentModel();

        // When
        String viewName = controller.generateSnippet("test-id", model);

        // Then
        assertThat(viewName).isEqualTo("replay/test-snippets :: warning");
    }

    @Test
    void shouldHandleEmptyRequestList() {
        // Given
        when(repository.findAll()).thenReturn(List.of());
        Model model = new ConcurrentModel();

        // When
        String viewName = controller.dashboard(model);

        // Then
        assertThat(viewName).isEqualTo("replay/dashboard");
        assertThat(model.getAttribute("requests")).isEqualTo(List.of());
    }
}


package de.hofmannhbm.replay;

import de.hofmannhbm.replay.core.ReplayRequestRepository;
import de.hofmannhbm.replay.generator.TestGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/replay")
@ConditionalOnProperty(prefix = "replay", name = "dashboardEnabled", havingValue = "true")
public class ReplayDashboardController {

    private static final Logger log = LoggerFactory.getLogger(ReplayDashboardController.class);
    private static final int MAX_ID_LENGTH = 100;

    private final ReplayRequestRepository repository;
    private final TestGenerationService generationService;

    public ReplayDashboardController(ReplayRequestRepository repository, TestGenerationService generationService) {
        this.repository = repository;
        this.generationService = generationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("requests", repository.findAll());
        return "replay/dashboard";
    }

    @GetMapping("/requests-table")
    public String getRequestsTable(Model model) {
        model.addAttribute("requests", repository.findAll());
        return "replay/dashboard :: request-table-rows";
    }

    @GetMapping("/details/{id}")
    public String getRequestDetails(@PathVariable("id") String id, Model model) {
        if (id == null || id.isBlank()) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "Die ID darf nicht leer sein.");
            return "replay/request-details :: error";
        }
        
        if (id.length() > MAX_ID_LENGTH) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "Die ID ist zu lang (max. " + MAX_ID_LENGTH + " Zeichen).");
            return "replay/request-details :: error";
        }
        
        var request = repository.findById(id);

        if (request == null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "Der Request mit ID '" + id + "' konnte nicht gefunden werden.");
            return "replay/request-details :: error";
        }

        model.addAttribute("request", request);
        return "replay/request-details :: details";
    }

    @GetMapping("/generate/{id}")
    public String generateSnippet(@PathVariable("id") String id, Model model) {
        log.debug("Generate request for ID: {}", id);

        if (id == null || id.isBlank()) {
            model.addAttribute("errorMessage", "Die ID darf nicht leer sein.");
            return "replay/test-snippets :: error";
        }
        
        if (id.length() > MAX_ID_LENGTH) {
            model.addAttribute("errorMessage", "Die ID ist zu lang (max. " + MAX_ID_LENGTH + " Zeichen).");
            return "replay/test-snippets :: error";
        }

        var request = repository.findById(id);

        if (request == null) {
            log.warn("Request not found for ID: {}", id);
            model.addAttribute("errorMessage", "Der Request mit ID '" + id + "' konnte nicht gefunden werden.");
            return "replay/test-snippets :: error";
        }

        log.debug("Generating tests for request: {} {}", request.method(), request.path());
        var snippets = generationService.generateAllFormats(request);
        log.debug("Generated {} test snippets", snippets.size());

        if (snippets.isEmpty()) {
            return "replay/test-snippets :: warning";
        }

        model.addAttribute("snippets", snippets.entrySet());
        log.debug("Returning snippets template with {} entries", snippets.size());

        return "replay/test-snippets :: snippets";
    }
}
package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.service.VsacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/vsac")
@Tag(name = "VSAC-Controller",
        description = "API for acquiring TicketGrantingTicket from VSAC Service for Testing and Integration Purposes.")
@Slf4j
public class VSACController {
    private final VsacService vsacService;

    public VSACController(VsacService vsacService) {
        this.vsacService = vsacService;
    }

    @Operation(summary = "Get Granting Ticket from VSAC",
            description = "Ticket to apply to other MAT FHIR Services Requiring VSAC integration and testing purposes")
    @GetMapping(path = "/getTicketGrantingTicket")
    public String getTicketGrantingTicket(
            @RequestParam String username,
            @RequestParam String password) {
        return vsacService.getGrantingTicket(username, password);
    }    
}

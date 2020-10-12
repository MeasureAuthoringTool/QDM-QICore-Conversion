package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.vsac.VsacService;
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
        description = "API for acquiring Tickets from VSAC Service for Testing and Integration Purposes.")
@Slf4j
public class VSACController {
    private final VsacService vsacService;

    public VSACController(VsacService vsacService) {
        this.vsacService = vsacService;
    }

    @Operation(summary = "Get Ticket Granting TIcket from VSAC",
            description = "Gets the 8 hour ticket that can be used to get single use service tickets.")
    @GetMapping(path = "/getTicketGrantingTicket")
    public String getGrantingTicket(@RequestParam String apiKey) {
        return vsacService.getTicketGrantingTicket(apiKey);
    }

    @Operation(summary = "Get service ticket from VSAC",
            description = "Gets the 5 minute singlue use ticket from VSAC for a ticket granting ticket.")
    @GetMapping(path = "/getServiceTicket")
    public String getSingleUseTicket(@RequestParam String ticketGrantingTicket) {
        return vsacService.getServiceTicket(ticketGrantingTicket);
    }
}

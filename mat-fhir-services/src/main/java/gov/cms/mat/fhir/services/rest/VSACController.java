/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.components.vsac.VsacClient;
import gov.cms.mat.fhir.services.components.vsac.VsacConverter;
import gov.cms.mat.fhir.services.service.VsacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author duanedecouteau
 */
@RestController
@RequestMapping(path = "/vsac")
@Tag(name = "VSAC-Controller", description = "API for aquiring TicketGrantingTicket from VSAC Service for Testing and Integration Purposes.")
@Slf4j
public class VSACController {
    private final VsacService vsacService;
    private final VsacClient vsacClient;
    private final VsacConverter vsacConverter;
    
    public VSACController(VsacService vsacService, VsacClient vsacClient, VsacConverter vsacConverter) {
        this.vsacService = vsacService;
        this.vsacClient = vsacClient;
        this.vsacConverter = vsacConverter;
    }
    
    @Operation(summary = "Get Granting Ticket from VSAC",
            description = "Ticket to apply to other MAT FHIR Services Requiring VSAC integration and testing purposes")
    @GetMapping(path = "/getTicketGrantingTicket")
    public String getTicketGrantingTicket (
                @RequestParam String username,
                @RequestParam String passwd) {
        String ticket = vsacService.getGrantingTicket(username, passwd);
        return ticket;
    }    
}

package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.vsac.VsacService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VSACControllerTest {
    @Mock
    private VsacService vsacService;

    @InjectMocks
    private VSACController vsacController;

    @Test
    void getGrantingTicket() {
        when(vsacService.getTicketGrantingTicket("apiKey")).thenReturn("grantingTicket");

        assertEquals("grantingTicket", vsacController.getGrantingTicket("apiKey"));
    }

    @Test
    void getSingleUseTicket() {
        when(vsacService.getServiceTicket("grantingTicket", "api-key")).thenReturn("serviceTicket");

        assertEquals("serviceTicket", vsacController.getSingleUseTicket("grantingTicket", "api-key"));
    }
}
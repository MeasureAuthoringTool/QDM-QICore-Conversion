package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.components.VsacClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VsacServiceTest {
    private static final String TOKEN = "token";
    private static final String PASS = "pass";
    private static final String USER_NAME = "user_name";
    private long TOKEN_SECS = 2;

    @Mock
    private VsacClient vsacClient;
    @InjectMocks
    private VsacServiceImpl vsacService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vsacService, "userName", USER_NAME);
        ReflectionTestUtils.setField(vsacService, "password", PASS);
        ReflectionTestUtils.setField(vsacService, "grantedTicketTimeOutSeconds", String.valueOf(TOKEN_SECS));
    }

    @Test
    void testValidateTicket_GrantingTicketFails() {
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(null);

        assertFalse(vsacService.validateTicket());

        verify(vsacClient).getGrantingTicket(USER_NAME, PASS); // calls granting and fails
        verify(vsacClient, never()).getServiceTicket(anyString()); // since granting fails no need to call service
    }

    @Test
    void testValidateTicket_ServiceTicketFails() {
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(TOKEN);
        when(vsacClient.getServiceTicket(TOKEN)).thenReturn(null);

        assertFalse(vsacService.validateTicket());

        verify(vsacClient).getGrantingTicket(USER_NAME, PASS); // calls granting and succeeds
        verify(vsacClient).getServiceTicket(TOKEN); // calls service and fails
    }

    @Test
    void testValidateTicket_ServiceTicketSucceeds() {
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(TOKEN);
        when(vsacClient.getServiceTicket(TOKEN)).thenReturn("service");

        assertTrue(vsacService.validateTicket());

        verify(vsacClient).getGrantingTicket(USER_NAME, PASS); // calls granting and succeeds
        verify(vsacClient).getServiceTicket(TOKEN); // calls service and fails
    }


    @Test
    void testValidateUser_GrantingTicketFails() {
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(null);
        assertFalse(vsacService.validateUser());
        verify(vsacClient).getGrantingTicket(USER_NAME, PASS);

        assertFalse(vsacService.validateUser());
        verify(vsacClient, times(2)).getGrantingTicket(USER_NAME, PASS); // should try again
    }

    @Test
    void testValidateUser_GrantingTicketSuccess() throws InterruptedException {
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(TOKEN);
        assertTrue(vsacService.validateUser());
        verify(vsacClient).getGrantingTicket(USER_NAME, PASS);

        assertTrue(vsacService.validateUser());
        verify(vsacClient).getGrantingTicket(USER_NAME, PASS); // wont try again our 2 sec token still valid

        TimeUnit.SECONDS.sleep(TOKEN_SECS);

        assertTrue(vsacService.validateUser());
        verify(vsacClient, times(2)).getGrantingTicket(USER_NAME, PASS); // token expired call again
    }
}
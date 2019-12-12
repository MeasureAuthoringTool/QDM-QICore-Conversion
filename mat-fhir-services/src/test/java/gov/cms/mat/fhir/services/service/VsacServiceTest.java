package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.components.vsac.VsacClient;
import gov.cms.mat.fhir.services.components.vsac.VsacConverter;
import gov.cms.mat.fhir.services.summary.VsacService;
import mat.model.VSACValueSetWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.vsac.VSACResponseResult;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VsacServiceTest {
    private static final String XML = "<xml/>";
    private static final String SERVICE_TOKEN = "service";
    private static final String TICKET_TOKEN = "ticket";
    private static final String OID = "oid";
    private static final String PASS = "pass";
    private static final String USER_NAME = "user_name";
    private long TOKEN_SECS = 1;

    @Mock
    private VsacConverter vsacConverter;
    @Mock
    private VsacClient vsacClient;
    @InjectMocks
    private VsacService vsacService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vsacService, "userName", USER_NAME);
        ReflectionTestUtils.setField(vsacService, "password", PASS);
        ReflectionTestUtils.setField(vsacService, "grantedTicketTimeOutSeconds", String.valueOf(TOKEN_SECS));
        ReflectionTestUtils.setField(vsacService, "serviceTicketTimeOutSeconds", String.valueOf(TOKEN_SECS));
    }

    @Test
    void testGetData_Success() {
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(SERVICE_TOKEN);
        when(vsacClient.getServiceTicket(SERVICE_TOKEN)).thenReturn(TICKET_TOKEN);

        VSACValueSetWrapper vsacValueSetWrapper = new VSACValueSetWrapper();
        when(vsacConverter.toWrapper(XML)).thenReturn(vsacValueSetWrapper);

        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setXmlPayLoad(XML);
        vsacResponseResult.setIsFailResponse(false);

        when(vsacClient.getDataFromProfile(OID, TICKET_TOKEN)).thenReturn(vsacResponseResult);

        assertEquals(vsacValueSetWrapper, vsacService.getData(OID));

        verify(vsacConverter).toWrapper(XML);
    }

    @Test
    void testGetData_InvalidUser() {
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(null);

        assertNull(vsacService.getData(OID));

        verify(vsacClient).getGrantingTicket(USER_NAME, PASS); // calls granting and fails makes getting data pointless
        verify(vsacClient, never()).getDataFromProfile(anyString(), anyString());
    }

    @Test
    void testGetData_InvalidVSACResponseResultNull() {
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(SERVICE_TOKEN);
        when(vsacClient.getServiceTicket(SERVICE_TOKEN)).thenReturn(TICKET_TOKEN);

        when(vsacClient.getDataFromProfile(OID, TICKET_TOKEN)).thenReturn(null);
        assertNull(vsacService.getData(OID));

        verify(vsacClient).getDataFromProfile(OID, TICKET_TOKEN);
    }

    @Test
    void testGetData_InvalidVSACResponseResult_MissingXML() {
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(SERVICE_TOKEN);
        when(vsacClient.getServiceTicket(SERVICE_TOKEN)).thenReturn(TICKET_TOKEN);

        VSACResponseResult vsacResponseResult = new VSACResponseResult();

        when(vsacClient.getDataFromProfile(OID, TICKET_TOKEN)).thenReturn(vsacResponseResult);

        assertNull(vsacService.getData(OID));
    }

    @Test
    void testGetData_ValidateTicket_InvalidVSACResponseResult_FailResponse() {
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(SERVICE_TOKEN);
        when(vsacClient.getServiceTicket(SERVICE_TOKEN)).thenReturn(TICKET_TOKEN);

        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setXmlPayLoad(XML);
        vsacResponseResult.setIsFailResponse(true);

        when(vsacClient.getDataFromProfile(OID, TICKET_TOKEN)).thenReturn(vsacResponseResult);

        assertNull(vsacService.getData(OID));

        verify(vsacConverter, never()).toWrapper(XML);
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
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(SERVICE_TOKEN);
        when(vsacClient.getServiceTicket(SERVICE_TOKEN)).thenReturn(null);

        assertFalse(vsacService.validateTicket());

        verify(vsacClient).getGrantingTicket(USER_NAME, PASS); // calls granting and succeeds
        verify(vsacClient).getServiceTicket(SERVICE_TOKEN); // calls service and fails
    }

    @Test
    void testValidateTicket_ServiceTicketSucceeds() throws InterruptedException {
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(SERVICE_TOKEN);
        when(vsacClient.getServiceTicket(SERVICE_TOKEN)).thenReturn(TICKET_TOKEN);

        assertTrue(vsacService.validateTicket());

        verify(vsacClient).getGrantingTicket(USER_NAME, PASS); // calls granting and succeeds
        verify(vsacClient).getServiceTicket(SERVICE_TOKEN); // calls service and succeeds

        assertTrue(vsacService.validateTicket());
        verify(vsacClient).getServiceTicket(SERVICE_TOKEN); // still valid only call once

        TimeUnit.SECONDS.sleep(TOKEN_SECS); // sleep time and make service ticket invalid

        assertTrue(vsacService.validateTicket());

        verify(vsacClient, times(2)).getServiceTicket(SERVICE_TOKEN); // since expired call again
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
        when(vsacClient.getGrantingTicket(USER_NAME, PASS)).thenReturn(SERVICE_TOKEN);
        assertTrue(vsacService.validateUser());
        verify(vsacClient).getGrantingTicket(USER_NAME, PASS);

        assertTrue(vsacService.validateUser());
        verify(vsacClient).getGrantingTicket(USER_NAME, PASS); // wont try again our 2 sec token still valid

        TimeUnit.SECONDS.sleep(TOKEN_SECS);

        assertTrue(vsacService.validateUser());
        verify(vsacClient, times(2)).getGrantingTicket(USER_NAME, PASS); // token expired call again
    }
}
package gov.cms.mat.fhir.services.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vsac.VSACResponseResult;

import gov.cms.mat.fhir.services.components.vsac.VsacClient;
import gov.cms.mat.fhir.services.components.vsac.VsacConverter;
import mat.model.VSACValueSetWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void testGetData_Success() {
        when(vsacClient.getServiceTicket(TICKET_TOKEN)).thenReturn(SERVICE_TOKEN);

        VSACValueSetWrapper vsacValueSetWrapper = new VSACValueSetWrapper();
        when(vsacConverter.toWrapper(XML)).thenReturn(vsacValueSetWrapper);

        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setXmlPayLoad(XML);
        vsacResponseResult.setIsFailResponse(false);

        when(vsacClient.getDataFromProfile(OID, SERVICE_TOKEN)).thenReturn(vsacResponseResult);

        assertEquals(vsacValueSetWrapper, vsacService.getData(OID, TICKET_TOKEN));

        verify(vsacConverter).toWrapper(XML);
    }

    @Test
    void testGetData_InvalidVSACResponseResultNull() {
        when(vsacClient.getServiceTicket(TICKET_TOKEN)).thenReturn(SERVICE_TOKEN);

        when(vsacClient.getDataFromProfile(OID, SERVICE_TOKEN)).thenReturn(null);
        assertNull(vsacService.getData(OID, TICKET_TOKEN));

        verify(vsacClient).getDataFromProfile(OID, SERVICE_TOKEN);
    }

    @Test
    void testGetData_InvalidVSACResponseResult_MissingXML() {
        when(vsacClient.getServiceTicket(TICKET_TOKEN)).thenReturn(SERVICE_TOKEN);

        VSACResponseResult vsacResponseResult = new VSACResponseResult();

        when(vsacClient.getDataFromProfile(OID, SERVICE_TOKEN)).thenReturn(vsacResponseResult);

        assertNull(vsacService.getData(OID, TICKET_TOKEN));
    }

    @Test
    void testGetData_ValidateTicket_InvalidVSACResponseResult_FailResponse() {
        when(vsacClient.getServiceTicket(TICKET_TOKEN)).thenReturn(SERVICE_TOKEN);

        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setXmlPayLoad(XML);
        vsacResponseResult.setIsFailResponse(true);

        when(vsacClient.getDataFromProfile(OID, SERVICE_TOKEN)).thenReturn(vsacResponseResult);

        assertNull(vsacService.getData(OID, TICKET_TOKEN));

        verify(vsacConverter, never()).toWrapper(XML);
    }

}
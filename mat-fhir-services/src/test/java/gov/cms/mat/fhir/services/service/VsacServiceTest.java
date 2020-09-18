package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.components.vsac.ValueSetVSACResponseResult;
import gov.cms.mat.fhir.services.components.vsac.VsacConverter;
import gov.cms.mat.fhir.services.components.vsac.VsacRestClient;
import mat.model.VSACValueSetWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VsacServiceTest {
    private static final String XML = "<xml/>";
    private static final String SERVICE_TOKEN = "service";
    private static final String OID = "oid";

    @Mock
    private VsacConverter vsacConverter;
    @Mock
    private VsacRestClient vsacRestClient;
    @InjectMocks
    private VsacService vsacService;

    @Test
    void testGetData_Success() {
        VSACValueSetWrapper vsacValueSetWrapper = new VSACValueSetWrapper();
        when(vsacConverter.toWrapper(XML)).thenReturn(vsacValueSetWrapper);

        ValueSetVSACResponseResult vsacResponseResult = ValueSetVSACResponseResult.builder()
                .xmlPayLoad(XML)
                .isFailResponse(false)
                .build();

        when(vsacRestClient.getDataFromProfile(OID, SERVICE_TOKEN)).thenReturn(vsacResponseResult);

        assertEquals(vsacValueSetWrapper, vsacService.getVSACValueSetWrapper(OID, SERVICE_TOKEN));

        verify(vsacConverter).toWrapper(XML);
    }

    @Test
    void testGetData_InvalidVSACResponseResultNull() {
        when(vsacRestClient.getDataFromProfile(OID, SERVICE_TOKEN)).thenReturn(null);
        assertNull(vsacService.getVSACValueSetWrapper(OID, SERVICE_TOKEN));

        verify(vsacRestClient).getDataFromProfile(OID, SERVICE_TOKEN);
    }

    @Test
    void testGetData_InvalidVSACResponseResult_MissingXML() {
        ValueSetVSACResponseResult vsacResponseResult = ValueSetVSACResponseResult.builder()
                .isFailResponse(true)
                .build();

        when(vsacRestClient.getDataFromProfile(OID, SERVICE_TOKEN)).thenReturn(vsacResponseResult);

        assertNull(vsacService.getVSACValueSetWrapper(OID, SERVICE_TOKEN));
    }

    @Test
    void testGetData_ValidateTicket_InvalidVSACResponseResult_FailResponse() {
        ValueSetVSACResponseResult vsacResponseResult = ValueSetVSACResponseResult.builder()
                .xmlPayLoad(XML)
                .isFailResponse(true)
                .build();

        when(vsacRestClient.getDataFromProfile(OID, SERVICE_TOKEN)).thenReturn(vsacResponseResult);

        assertNull(vsacService.getVSACValueSetWrapper(OID, SERVICE_TOKEN));

        verify(vsacConverter, never()).toWrapper(XML);
    }
}
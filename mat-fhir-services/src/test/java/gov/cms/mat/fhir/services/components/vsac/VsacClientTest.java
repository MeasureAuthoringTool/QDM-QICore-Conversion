package gov.cms.mat.fhir.services.components.vsac;

import gov.cms.mat.fhir.services.config.VsacConfig;
import gov.cms.mat.vsac.VsacRestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vsac.VSACResponseResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class VsacClientTest {
    private static final String OID = "oid";
    private static final String SERVICE_TICKET = "ticket";
    private static final String XML = "<xml>XML</xml>";

    @Mock
    private VsacConfig vsacConfig;
    @Mock
    private VsacRestClient vGroovyClient;
    @InjectMocks
    private VsacClient vsacClient;


    @Test
    void getDataFromProfile_NoCacheBadResponse() {
        when(vsacConfig.getVsacRestClient()).thenReturn(vGroovyClient);

        VSACResponseResult toReturn = createResponseResult(true, null);

        VSACResponseResult returnedFromClient = vsacClient.getDataFromProfile(OID, SERVICE_TICKET);

        assertEquals(toReturn, returnedFromClient);

        verify(vGroovyClient).getVsacDataForConversion(OID, SERVICE_TICKET, VsacClient.PROFILE);
    }


    private VSACResponseResult createResponseResult(boolean isFail, String xml) {
        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setIsFailResponse(isFail);
        vsacResponseResult.setXmlPayLoad(xml);

        when(vGroovyClient.getVsacDataForConversion(OID, SERVICE_TICKET, VsacClient.PROFILE)).thenReturn(vsacResponseResult);

        return vsacResponseResult;
    }
}
package gov.cms.mat.fhir.services.components.vsac;

import gov.cms.mat.fhir.services.config.VsacConfig;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@Disabled // integration test
class VsacRestClientIntegrationTest
{
    private final RestTemplate restTemplate = new RestTemplate();
    VsacRestClient vsacRestClient;
    String grantingTicket = "";

    @BeforeEach
    void setUp() {
        VsacConfig vsacConfig = new VsacConfig();
        vsacConfig.setService("https://vsac.nlm.nih.gov");

        vsacRestClient = new VsacRestClient(restTemplate, vsacConfig);

       String userName = System.getenv("VSAC_USER");
        String userPass = System.getenv("VSAC_PASS");

        if (StringUtils.isEmpty(grantingTicket)) {
            grantingTicket = vsacRestClient.fetchGrantingTicket(userName, userPass);
        }
    }

    @Test
    void fetchGrantingTicket() {
        assertNotNull(grantingTicket);
    }

    @Test
    void fetchSingleUseTicket() {
        String singleUseTicket = vsacRestClient.fetchSingleUseTicket(grantingTicket);

        assertNotNull(singleUseTicket);
    }

    @Test
    void fetchCodeSystem() {
        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem("/CodeSystem/LOINC/Version/2.67/Code/21112-8/Info", grantingTicket);

        assertEquals("ok", vsacResponse.getStatus());
        assertNull(vsacResponse.getErrors());
    }

    @Test
    void fetchCodeSystemNoVersion() {
        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem("/CodeSystem/LOINC/Code/21112-8/Info", grantingTicket);

        assertEquals("404", vsacResponse.getStatus());
        assertNull(vsacResponse.getErrors());
    }

    @Test
    void fetchCodeInvalidCodeSystem() {
        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem("/CodeSystem/LOINC333333/Version/2.67/Code/21112-8/Info", grantingTicket);

        assertEquals("error", vsacResponse.getStatus());
        assertEquals(1, vsacResponse.getErrors().getErrorCount());
        assertEquals(1, vsacResponse.getErrors().getResultSet().size());

        VsacResponse.VsacErrorResultSet errorResultSet = vsacResponse.getErrors().getResultSet().get(0);
        assertEquals("CodeSystem not found.", errorResultSet.getErrDesc());
        assertEquals("800", errorResultSet.getErrCode());

    }

    @Test
    void fetchCodeInvalidCode() {
        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem("/CodeSystem/LOINC/Version/2.67/Code/21112-811111111/Info", grantingTicket);

        assertEquals("error", vsacResponse.getStatus());
        assertEquals(1, vsacResponse.getErrors().getErrorCount());
        assertEquals(1, vsacResponse.getErrors().getResultSet().size());

        VsacResponse.VsacErrorResultSet errorResultSet = vsacResponse.getErrors().getResultSet().get(0);
        assertEquals("Code not found.", errorResultSet.getErrDesc());
        assertEquals("802", errorResultSet.getErrCode());
    }

    @Test
    void fetchCodeInvalidCodeSystemVersion() {
        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem("/CodeSystem/LOINC/Version/2.67999900/Code/21112-8/Info", grantingTicket);

        assertEquals("error", vsacResponse.getStatus());
        assertEquals(1, vsacResponse.getErrors().getErrorCount());
        assertEquals(1, vsacResponse.getErrors().getResultSet().size());

        VsacResponse.VsacErrorResultSet errorResultSet = vsacResponse.getErrors().getResultSet().get(0);
        assertEquals("Version not found.", errorResultSet.getErrDesc());
        assertEquals("801", errorResultSet.getErrCode());
    }

    @Test
    void fetchCodeSystemInfo() {
        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem("/CodeSystem/LOINC/Info", grantingTicket);

        assertEquals("ok", vsacResponse.getStatus());
        assertNull(vsacResponse.getErrors());
        assertEquals(1, vsacResponse.getData().getResultCount());
        assertEquals(1, vsacResponse.getData().getResultSet().size());

        VsacResponse.VsacDataResultSet vsacDataResultSet = vsacResponse.getData().getResultSet().get(0);

        assertEquals("LOINC", vsacDataResultSet.getCsName());
        assertEquals("2.67", vsacDataResultSet.getCsVersion());
        assertEquals("Complete", vsacDataResultSet.getContentMode());

        assertNull(vsacDataResultSet.getCsOID());
        assertNull(vsacDataResultSet.getCode());
        assertNull(vsacDataResultSet.getCodeName());
        assertNull(vsacDataResultSet.getTermType());
        assertNull(vsacDataResultSet.getActive());
        assertNull(vsacDataResultSet.getRevision());
    }
}
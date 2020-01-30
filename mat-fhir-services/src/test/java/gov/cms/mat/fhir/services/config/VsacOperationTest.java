package gov.cms.mat.fhir.services.config;

import gov.cms.mat.fhir.services.service.VsacService;
import mat.model.VSACValueSetWrapper;
import mat.model.cql.CQLQualityDataSetDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
@ActiveProfiles("test")
class VsacOperationTest {
    // when the vsac connect timeout's the groovy client will retry, making testing impossible.
    private final static boolean RUN_VSAC_INTEGRATION_TESTS = false;

    @Autowired
    private VsacConfig vsacConfig;
    @Autowired
    private VsacService vsacService;
    @Autowired
    private Environment environment;

    // @Test
    void testConfigProperties() {


        assertEquals("https://vsac.nlm.nih.gov/vsac/ws/Ticket", vsacConfig.getServer());
        assertEquals("http://umlsks.nlm.nih.gov", vsacConfig.getService());
        assertEquals("https://vsac.nlm.nih.gov/vsac/svs/RetrieveMultipleValueSets?", vsacConfig.getRetrieveMultiOidsService());
        assertEquals("https://vsac.nlm.nih.gov/vsac/profiles", vsacConfig.getProfileService());
        assertEquals("https://vsac.nlm.nih.gov/vsac/oid/", vsacConfig.getVersionService());
        assertEquals("https://vsac.nlm.nih.gov/vsac", vsacConfig.getVsacServerDrcUrl());
    }

    // @Test
    void testValidateUser() {
        if (haveVsacCredentialsInEnvironmentAndFlag()) {
            assertTrue(vsacService.validateUser());
            assertTrue(vsacService.validateTicket());
        }
    }

    // @Test
    void testValidateData() {
        if (haveVsacCredentialsInEnvironmentAndFlag()) {
            CQLQualityDataSetDTO cqlQualityDataSetDTO = new CQLQualityDataSetDTO();
            cqlQualityDataSetDTO.setOid("2.16.840.1.113762.1.4.1195.291");
            //  cqlQualityDataSetDTO.setVersion("20190129"); version and revision ignored

            {
                VSACValueSetWrapper valueSetWrapper = vsacService.getData(cqlQualityDataSetDTO.getOid());
                assertNotNull(valueSetWrapper);
            }

            { // must fetch the
                VSACValueSetWrapper valueSetWrapper = vsacService.getData(cqlQualityDataSetDTO.getOid());
                assertNotNull(valueSetWrapper);
            }
        }
    }

    private boolean haveVsacCredentialsInEnvironmentAndFlag() {
        if (RUN_VSAC_INTEGRATION_TESTS) {
            boolean haveProperties =
                    StringUtils.isNotBlank(environment.getProperty("VSAC_USER")) ||
                            StringUtils.isNotBlank(environment.getProperty("VSAC_PASS"));

            if (!haveProperties) {
                // wont fail build if not found when running in jenkins
                System.out.println("CANNOT run vsac test! Environment vars VSAC_USER and/or VSAC_PASS not set.");
            }

            return haveProperties;
        } else {
            System.out.println("RUN_VSAC_INTEGRATION_TESTS flag is off");
            return false;
        }
    }
}

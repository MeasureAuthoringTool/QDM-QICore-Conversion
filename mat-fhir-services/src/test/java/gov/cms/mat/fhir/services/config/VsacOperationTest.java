package gov.cms.mat.fhir.services.config;

import gov.cms.mat.fhir.services.components.vsac.VsacRestClient;
import gov.cms.mat.fhir.services.service.VsacService;
import mat.model.VSACValueSetWrapper;
import mat.model.cql.CQLQualityDataSetDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Disabled
class VsacOperationTest {
    // when the vsac connect timeout's the groovy client will retry, making testing impossible.
    private final static boolean RUN_VSAC_INTEGRATION_TESTS = true;

    @Autowired
    private VsacConfig vsacConfig;
    @Autowired
    private VsacRestClient vsacClient;
    @Autowired
    private VsacService vsacService;
    @Autowired
    private Environment environment;

    @Test
    void testConfigProperties() {
        assertEquals("http://umlsks.nlm.nih.gov", vsacConfig.getService());
    }

    @Test
    void testValidateData() {
        if (haveVsacCredentialsInEnvironmentAndFlag()) {
            String vsacGrantingTicket = vsacClient.fetchGrantingTicket(getVsacUser(), getVsacPass());
            CQLQualityDataSetDTO cqlQualityDataSetDTO = new CQLQualityDataSetDTO();
            cqlQualityDataSetDTO.setOid("2.16.840.1.113762.1.4.1116.180");
            //  cqlQualityDataSetDTO.setVersion("20190129"); version and revision ignored

            {
                VSACValueSetWrapper valueSetWrapper = vsacService.getVSACValueSetWrapper(cqlQualityDataSetDTO.getOid(), vsacGrantingTicket);
                assertNotNull(valueSetWrapper);
            }

            { // must fetch the
                VSACValueSetWrapper valueSetWrapper = vsacService.getVSACValueSetWrapper(cqlQualityDataSetDTO.getOid(), vsacGrantingTicket);
                assertNotNull(valueSetWrapper);
            }
        }
    }

    private boolean haveVsacCredentialsInEnvironmentAndFlag() {
        if (RUN_VSAC_INTEGRATION_TESTS) {
            boolean haveProperties =
                    StringUtils.isNotBlank(getVsacUser()) ||
                            StringUtils.isNotBlank(getVsacPass());

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

    private String getVsacPass() {
        return environment.getProperty("VSAC_PASS");
    }

    private String getVsacUser() {
        return environment.getProperty("VSAC_USER");
    }

}

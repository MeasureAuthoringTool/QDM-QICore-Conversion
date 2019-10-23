package gov.cms.mat.fhir.services.config;


import gov.cms.mat.fhir.services.service.VsacService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class VsacOperationTest {
    @Autowired
    private VsacConfig vsacConfig;
    @Autowired
    private VsacService vsacService;
    @Autowired
    private Environment environment;

    @Test
    void testConfigProperties() {
        assertEquals("https://vsac.nlm.nih.gov/vsac/ws/Ticket", vsacConfig.getServer());
        assertEquals("http://umlsks.nlm.nih.gov", vsacConfig.getService());
        assertEquals("https://vsac.nlm.nih.gov/vsac/svs/RetrieveMultipleValueSets?", vsacConfig.getRetrieveMultiOidsService());
        assertEquals("https://vsac.nlm.nih.gov/vsac/profiles", vsacConfig.getProfileService());
        assertEquals("https://vsac.nlm.nih.gov/vsac/oid/", vsacConfig.getVersionService());
        assertEquals("https://vsac.nlm.nih.gov/vsac", vsacConfig.getVsacServerDrcUrl());
    }

    @Test
    void testLogin() {
        if (StringUtils.isBlank(environment.getProperty("VSAC_USER")) || StringUtils.isBlank(environment.getProperty("VSAC_PASS"))) {
            // wont fail build if not found when running in jenkins
            System.out.println("CANNOT run vsac test! Environment vars VSAC_USER and/or VSAC_PASS not set.");
        } else {
            String data = vsacService.validateUser();
            assertNotNull(data);
        }
    }

}

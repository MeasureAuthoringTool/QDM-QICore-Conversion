package gov.cms.mat.fhir.services.config;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class VsacConfigTest {
    @Autowired
    private VsacConfig vsacConfig;

    @Test
    void getServer() {
        assertEquals("https://vsac.nlm.nih.gov/vsac/ws/Ticket", vsacConfig.getServer());
        assertEquals("http://umlsks.nlm.nih.gov", vsacConfig.getService());
        assertEquals("https://vsac.nlm.nih.gov/vsac/svs/RetrieveMultipleValueSets?", vsacConfig.getRetrieveMultiOidsService());
        assertEquals("https://vsac.nlm.nih.gov/vsac/profiles", vsacConfig.getProfileService());
        assertEquals("https://vsac.nlm.nih.gov/vsac/oid/", vsacConfig.getVersionService());
        assertEquals("https://vsac.nlm.nih.gov/vsac", vsacConfig.getVsacServerDrcUrl());
    }
}

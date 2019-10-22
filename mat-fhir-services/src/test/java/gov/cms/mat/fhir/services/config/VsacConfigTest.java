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
        assertEquals("https://server", vsacConfig.getServer());
        assertEquals("https://service", vsacConfig.getService());
        assertEquals("https://oids", vsacConfig.getRetrieveMultiOidsService());
        assertEquals("https://profile", vsacConfig.getProfileService());
        assertEquals("https://version", vsacConfig.getVersionService());
        assertEquals("https://drc", vsacConfig.getVsacServerDrcUrl());
        assertEquals("mat.server.twofactorauth.DefaultOTPValidatorForUser", vsacConfig.getAuthCLass());
    }
}

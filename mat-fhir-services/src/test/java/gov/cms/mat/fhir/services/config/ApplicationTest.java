package gov.cms.mat.fhir.services.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTest {
    @Autowired
    LibraryConversionFileConfig libraryConversionFileConfig;

    @Test
    void contextLoads() {
        assertFalse(libraryConversionFileConfig.getOrder().isEmpty());
    }
}
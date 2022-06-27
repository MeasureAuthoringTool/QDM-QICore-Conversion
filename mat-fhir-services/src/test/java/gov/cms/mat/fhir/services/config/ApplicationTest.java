package gov.cms.mat.fhir.services.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTest {
    @Autowired
    ConversionLibraryLookup conversionLibraryLookup;


    @Test
    void contextLoads() {
        assertEquals(8, conversionLibraryLookup.getMap().size());
        assertEquals("4.1.000", conversionLibraryLookup.getMap().get("FHIRHelpers"));

        List<String> keys = new ArrayList<>(conversionLibraryLookup.getMap().keySet());

        assertEquals("FHIRHelpers", keys.get(0)); // Checks order is maintained
        assertEquals("Hospice", keys.get(3));
        assertEquals("VTEICU", keys.get(keys.size() - 1));
    }
}
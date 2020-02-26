package gov.cms.mat.qdmqicore.conversion;

import gov.cms.mat.qdmqicore.conversion.spread_sheet_data.FhirQdmMappingData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationTest {
    @Autowired
    private FhirQdmMappingData fhirQdmMappingData;

    @Test
    void contextLoads() {
        assertTrue(fhirQdmMappingData.areAllUnique());
    }
}
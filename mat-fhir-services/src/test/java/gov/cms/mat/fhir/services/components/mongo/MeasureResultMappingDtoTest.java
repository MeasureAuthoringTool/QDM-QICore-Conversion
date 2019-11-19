package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.services.service.support.ConversionMapping;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MeasureResultMappingDtoTest {
    @Test
    void testEquals() {
        ConversionResult.MeasureResult result = ConversionResult.MeasureResult.builder().build();
        ConversionMapping conversionMapping = ConversionMapping.builder().build();

        MeasureResultMappingDto lhs = new MeasureResultMappingDto(result, conversionMapping);
        MeasureResultMappingDto rhs = new MeasureResultMappingDto(result, conversionMapping);

        assertEquals(lhs, rhs);
        assertNotEquals(lhs, new MeasureResultMappingDto(result, "ERROR"));
    }
}
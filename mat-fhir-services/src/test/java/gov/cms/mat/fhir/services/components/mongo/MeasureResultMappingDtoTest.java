
package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.FieldConversionResult;
import gov.cms.mat.fhir.rest.dto.spreadsheet.MatAttribute;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MeasureResultMappingDtoTest {
    @Test
    void testEquals() {
        FieldConversionResult result = FieldConversionResult.builder().build();
        MatAttribute matAttr = new MatAttribute();

        MeasureResultMappingDto lhs = new MeasureResultMappingDto(result, matAttr);
        MeasureResultMappingDto rhs = new MeasureResultMappingDto(result, matAttr);

        assertEquals(lhs, rhs);
        assertNotEquals(lhs, new MeasureResultMappingDto(result, "ERROR"));
    }
}
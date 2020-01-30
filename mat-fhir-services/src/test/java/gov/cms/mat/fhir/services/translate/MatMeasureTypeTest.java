package gov.cms.mat.fhir.services.translate;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MatMeasureTypeTest {

    @Test
    void findByMatAbbreviation() {
        assertNull(findByMatAbbreviation(null));
        assertNull(findByMatAbbreviation("BAD--ABBREV"));

        assertEquals(MatMeasureType.COMPOSITE, findByMatAbbreviation("COMPOSITE"));

        assertEquals(MatMeasureType.INTERM_OM, findByMatAbbreviation("INTERM-OM"));
        assertEquals(MatMeasureType.OUTCOME, findByMatAbbreviation("OUTCOME"));

        assertEquals(MatMeasureType.PRO_PM, findByMatAbbreviation("PRO-PM"));

        assertEquals(MatMeasureType.STRUCTURE, findByMatAbbreviation("STRUCTURE"));
        assertEquals(MatMeasureType.RESOURCE, findByMatAbbreviation("RESOURCE"));

        assertEquals(MatMeasureType.APPROPRIATE, findByMatAbbreviation("APPROPRIATE"));
        assertEquals(MatMeasureType.EFFICIENCY, findByMatAbbreviation("EFFICIENCY"));
        assertEquals(MatMeasureType.PROCESS, findByMatAbbreviation("PROCESS"));
    }

    @Test
    void verifyProcessType() {
        MatMeasureType[] types = new MatMeasureType[]{MatMeasureType.APPROPRIATE, MatMeasureType.EFFICIENCY,
                MatMeasureType.PROCESS};

        verifyType(types, MatMeasureType.FhirCodes.PROCESS);
    }

    @Test
    void verifyOutcomeType() {
        MatMeasureType[] types = new MatMeasureType[]{MatMeasureType.INTERM_OM, MatMeasureType.OUTCOME};

        verifyType(types, MatMeasureType.FhirCodes.OUTCOME);
    }

    @Test
    void verifyStructureType() {
        MatMeasureType[] types = new MatMeasureType[]{MatMeasureType.RESOURCE, MatMeasureType.STRUCTURE};

        verifyType(types, MatMeasureType.FhirCodes.STRUCTURE);
    }

    private void verifyType(MatMeasureType[] types, String code) {
        Arrays.stream(types)
                .forEach(t -> assertEquals(t.fhirCode, code));
    }

    private MatMeasureType findByMatAbbreviation(String abbrName) {
        return MatMeasureType.findByMatAbbreviation(abbrName).orElse(null);
    }
}
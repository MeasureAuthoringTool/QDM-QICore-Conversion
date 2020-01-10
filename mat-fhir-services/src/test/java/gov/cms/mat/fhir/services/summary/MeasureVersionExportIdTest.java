package gov.cms.mat.fhir.services.summary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeasureVersionExportIdTest {
    @Test
    void testToString() {
        String id = "ID";
        String version = "VERSION";

        MeasureVersionExportId measureVersionExportId = new MeasureVersionExportId(id, version);
        assertEquals(id, measureVersionExportId.getId());
        assertEquals(version, measureVersionExportId.getVersion());

        assertTrue(measureVersionExportId.toString().contains(id));
        assertTrue(measureVersionExportId.toString().contains(version));
    }
}
package gov.cms.mat.qdmqicore.conversion.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchDataTest {
    @Test
    void builder() {
        SearchData searchData = SearchData.builder()
                .matDataTypeDescription("matDataTypeDescription")
                .fhirResource("fhirResource")
                .matAttributeName("matAttributeName")
                .fhirR4QiCoreMapping("fhirR4QiCoreMapping")
                .fhirElement("fhirElement")
                .fhirType("fhirType")
                .build();

        assertEquals("matDataTypeDescription", searchData.getMatDataTypeDescription());
        assertEquals("fhirResource", searchData.getFhirResource());
        assertEquals("matAttributeName", searchData.getMatAttributeName());
        assertEquals("fhirR4QiCoreMapping", searchData.getFhirR4QiCoreMapping());
        assertEquals("fhirElement", searchData.getFhirElement());
        assertEquals("fhirType", searchData.getFhirType());
    }
}
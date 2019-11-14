package gov.cms.mat.qdmqicore.conversion.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConversionMappingDataTest implements ConversionDataBuilder {

    @Test
    void builder() {
        ConversionMapping conversionMapping = buildConversionMapping();

        assertEquals("title", conversionMapping.getTitle());
        assertEquals("matDataTypeDescription", conversionMapping.getMatDataTypeDescription());
        assertEquals("matAttributeName", conversionMapping.getMatAttributeName());
        assertEquals("fhirR4QiCoreMapping", conversionMapping.getFhirR4QiCoreMapping());
        assertEquals("fhirResource", conversionMapping.getFhirResource());
        assertEquals("fhirElement", conversionMapping.getFhirElement());
        assertEquals("fhirType", conversionMapping.getFhirType());
    }
}
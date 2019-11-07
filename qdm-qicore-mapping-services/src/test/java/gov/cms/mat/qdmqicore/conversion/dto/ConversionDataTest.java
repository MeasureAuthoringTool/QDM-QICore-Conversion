package gov.cms.mat.qdmqicore.conversion.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.qdmqicore.conversion.spread_sheet_data.ConversionData;
import gov.cms.mat.qdmqicore.conversion.spread_sheet_data.ConversionEntry;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConversionDataTest {

    @Test
    void getFeed() throws JsonProcessingException {
        ConversionData conversionData = createConversionDataFromFile();

        ConversionData.Feed feed = conversionData.getFeed();
        assertEquals(595, feed.getEntry().size());

        ConversionEntry entry = feed.getEntry().get(0);

        // no null safe Data mapping method implementation
        assertEquals("1", entry.getMatDataTypeId().getData());
        assertEquals("398", entry.getMatAttributeId().getData());
        assertEquals("2019-11-06T14:39:34.529Z", entry.getUpdated().getData());

        assertEquals("Patient Care Experience", entry.getTitle().getData());
        assertEquals(entry.getTitle().getData(), entry.getTitleData());

        assertEquals("Patient Care Experience", entry.getMatDataTypeDescription().getData());
        assertEquals(entry.getMatDataTypeDescription().getData(), entry.getMatDataTypeDescriptionData());

        assertEquals("authorDatetime", entry.getMatAttributeName().getData());
        assertEquals(entry.getMatAttributeName().getData(), entry.getMatAttributeNameData());

        assertEquals("Observation.issued", entry.getFhirR4QiCoreMapping().getData());
        assertEquals(entry.getFhirR4QiCoreMapping().getData(), entry.getFhirR4QiCoreMappingData());

        assertEquals("Observation", entry.getFhirResource().getData());
        assertEquals(entry.getFhirResource().getData(), entry.getFhirResourceData());

        assertEquals("issued", entry.getFhirElement().getData());
        assertEquals(entry.getFhirElement().getData(), entry.getFhirElementData());

        assertEquals("instant", entry.getFhirType().getData());
        assertEquals(entry.getFhirType().getData(), entry.getFhirTypeData());
    }

    private ConversionData createConversionDataFromFile() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(getJson(), ConversionData.class);
    }

    private String getJson() {
        File inputXmlFile = new File(this.getClass().getResource("/spread_sheet_data.json").getFile());

        try {
            return new String(Files.readAllBytes(inputXmlFile.toPath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
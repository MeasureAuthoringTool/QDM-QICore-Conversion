package gov.cms.mat.qdmqicore.conversion.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleMatAttributes;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleMatAttributesData;
import gov.cms.mat.qdmqicore.mapping.model.google.GoogleMatAttributesFeed;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static gov.cms.mat.qdmqicore.mapping.utils.SpreadSheetUtils.getData;
import static org.junit.Assert.assertEquals;

class ConversionDataTest {

    @Test
    void getFeed() throws JsonProcessingException {
        GoogleMatAttributesData googleMatAtttributes = createConversionDataFromFile();

        GoogleMatAttributesFeed feed = googleMatAtttributes.getFeed();
        assertEquals(595, feed.getEntry().size());

        GoogleMatAttributes entry = feed.getEntry().get(0);

        // no null safe Data mapping method implementation
        assertEquals("1", entry.getMatDataTypeId().getData());
        assertEquals("398", entry.getMatAttributeId().getData());
        assertEquals("2019-11-06T14:39:34.529Z", entry.getUpdated().getData());

        assertEquals("Patient Care Experience", entry.getTitle().getData());
        assertEquals(entry.getTitle().getData(), getData(entry.getTitle()));

        assertEquals("Patient Care Experience", entry.getMatDataTypeDescription().getData());
        assertEquals(entry.getMatDataTypeDescription().getData(), getData(entry.getMatDataTypeDescription()));

        assertEquals("authorDatetime", entry.getMatAttributeName().getData());
        assertEquals(entry.getMatAttributeName().getData(), getData(entry.getMatAttributeName()));

        assertEquals("Observation.issued", entry.getFhirR4QiCoreMapping().getData());
        assertEquals(entry.getFhirR4QiCoreMapping().getData(), getData(entry.getFhirR4QiCoreMapping()));

        assertEquals("Observation", entry.getFhirResource().getData());
        assertEquals(entry.getFhirResource().getData(), getData(entry.getFhirResource()));

        assertEquals("issued", entry.getFhirElement().getData());
        assertEquals(entry.getFhirElement().getData(), getData(entry.getFhirElement()));

        assertEquals("instant", entry.getFhirType().getData());
        assertEquals(entry.getFhirType().getData(), getData(entry.getFhirType()));
    }

    private GoogleMatAttributesData createConversionDataFromFile() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(getJson(), GoogleMatAttributesData.class);
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
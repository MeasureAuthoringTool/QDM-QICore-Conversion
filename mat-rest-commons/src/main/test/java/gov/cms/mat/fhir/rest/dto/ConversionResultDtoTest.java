package gov.cms.mat.fhir.rest.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.ResourceFileUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConversionResultDtoTest implements ResourceFileUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parseJson() throws JsonProcessingException {
        String json = getStringFromResource("/report.json");
        ConversionResultDto conversionResultDto = objectMapper.readValue(json, ConversionResultDto.class);
        assertEquals("40280382649c54c30164d76256dd11dc", conversionResultDto.getMeasureId());
    }
}
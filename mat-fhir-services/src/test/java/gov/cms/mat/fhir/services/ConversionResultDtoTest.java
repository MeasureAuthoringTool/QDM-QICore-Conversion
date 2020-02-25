package gov.cms.mat.fhir.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConversionResultDtoTest implements ResourceFileUtil {

    @Test
    void builder() throws JsonProcessingException {
        String json = getStringFromResource("/conversion-result.json");

        ObjectMapper objectMapper = new ObjectMapper();
        ConversionResultDto conversionResultDto = objectMapper.readValue(json, ConversionResultDto.class);

        assertNotNull(conversionResultDto);
    }
}
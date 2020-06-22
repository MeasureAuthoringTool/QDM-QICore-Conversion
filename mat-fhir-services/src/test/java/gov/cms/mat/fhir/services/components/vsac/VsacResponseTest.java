package gov.cms.mat.fhir.services.components.vsac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VsacResponseTest implements ResourceFileUtil {

    @Test
    void getDataNoError() throws JsonProcessingException {
        String json = getStringFromResource("/vsacResponse.json");

        ObjectMapper mapper = new ObjectMapper();

        VsacResponse vsacResponse = mapper.readValue(json, VsacResponse.class);
        assertEquals("ok", vsacResponse.getMessage());
        assertEquals("ok", vsacResponse.getStatus());

        assertEquals(1, vsacResponse.getData().getResultCount());
        assertEquals(1, vsacResponse.getData().getResultSet().size());

        VsacResponse.VsacDataResultSet vsacDataResultSet = vsacResponse.getData().getResultSet().get(0);

        assertEquals("LOINC", vsacDataResultSet.getCsName());
        assertEquals("2.16.840.1.113883.6.1", vsacDataResultSet.getCsOID());

    }

    @Test
    void getDataError() throws JsonProcessingException {
        String json = getStringFromResource("/vsacResponseError.json");
        assertFalse(json.isEmpty());

        ObjectMapper mapper = new ObjectMapper();

        VsacResponse vsacResponse = mapper.readValue(json, VsacResponse.class);
        assertEquals("Errors getting code...", vsacResponse.getMessage());
        assertEquals("error", vsacResponse.getStatus());

        assertNull(vsacResponse.getData());

        assertEquals(1, vsacResponse.getErrors().getErrorCount());

        VsacResponse.VsacErrorResultSet vsacErrorResultSet = vsacResponse.getErrors().getResultSet().get(0);
        assertEquals("CodeSystem not found.", vsacErrorResultSet.getErrDesc());
        assertEquals("800", vsacErrorResultSet.getErrCode());

    }
}
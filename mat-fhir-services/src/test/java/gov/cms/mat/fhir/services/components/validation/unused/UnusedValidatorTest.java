package gov.cms.mat.fhir.services.components.validation.unused;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.cql.parser.CqlVisitorFactory;
import gov.cms.mat.fhir.services.rest.MatXmlController;
import gov.cms.mat.fhir.services.rest.dto.UnusedCqlElements;
import mat.model.cql.CQLModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnusedValidatorTest implements ResourceFileUtil {
    @Test
    void findUnusedValueSets() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String json = getStringFromResource("/unused/MatCqlXmlReq.json");
        MatXmlController.MatCqlXmlReq matCqlXmlReq = mapper.readValue(json, MatXmlController.MatCqlXmlReq.class);
        assertEquals(2, matCqlXmlReq.getSourceModel().getValueSetList().size());

        UnusedValidator unused = new UnusedValidator(matCqlXmlReq.getSourceModel());
        UnusedCqlElements unusedCqlElements = unused.findUnused();

        assertEquals(1, unusedCqlElements.getValueSets().size());
        assertEquals("2 Hour Blood Glucose Test", unusedCqlElements.getValueSets().get(0).getCodeListName());
    }

    @Test
    void findUnused() {
        CQLModel cqlModel = new CQLModel();
        UnusedValidator unusedValidator = new UnusedValidator(cqlModel);

        UnusedCqlElements unusedCqlElements = unusedValidator.findUnused();
        assertTrue(unusedCqlElements.getCodes().isEmpty());
        assertTrue(unusedCqlElements.getLibraries().isEmpty());
        assertTrue(unusedCqlElements.getValueSets().isEmpty());
    }
}
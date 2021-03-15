package gov.cms.mat.fhir.services.components.validation.unused;

import gov.cms.mat.fhir.services.rest.dto.UnusedCqlElements;
import mat.model.cql.CQLModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UnusedValidatorTest {
    /* Other unused validators tests have full coverage */
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
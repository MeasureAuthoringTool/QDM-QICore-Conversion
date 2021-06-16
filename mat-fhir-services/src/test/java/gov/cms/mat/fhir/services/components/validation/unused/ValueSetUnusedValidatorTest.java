package gov.cms.mat.fhir.services.components.validation.unused;

import mat.model.cql.CQLDefinition;
import mat.model.cql.CQLFunctions;
import mat.model.cql.CQLModel;
import mat.model.cql.CQLQualityDataSetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValueSetUnusedValidatorTest {
    private CQLModel cqlModel;
    private ValueSetUnusedValidator validator;

    CQLQualityDataSetDTO used;
    CQLQualityDataSetDTO unUsed;

    @BeforeEach
    void setUp() {
        cqlModel = new CQLModel();
        validator = new ValueSetUnusedValidator(cqlModel);

        used = createValueSet("used");
        unUsed = createValueSet("unused");
    }

    @Test
    void findUnusedNoEntries() {
        assertTrue(validator.findUnused().isEmpty());
    }

    @Test
    void findUnusedInFunctions() {
        cqlModel.setValueSetList(List.of(used, unUsed));

        CQLFunctions cqlFunction = new CQLFunctions();
        cqlFunction.setFunctionLogic("[MedicationRequest: \"used\"] Opioids");
        cqlModel.setCqlFunctions(List.of(cqlFunction));

        List<CQLQualityDataSetDTO> unused = validator.findUnused();
        assertEquals(1, unused.size());
        assertEquals(unUsed, unused.get(0));
    }

    @Test
    void findUnusedInDefines() {
        cqlModel.setValueSetList(List.of(used, unUsed));

        CQLDefinition cqlDefinition = new CQLDefinition();
        cqlDefinition.setDefinitionLogic("[MedicationRequest: \"used\"] Opioids");
        cqlModel.setDefinitionList(List.of(cqlDefinition));

        List<CQLQualityDataSetDTO> unused = validator.findUnused();
        assertEquals(1, unused.size());
        assertEquals(unUsed, unused.get(0));
    }

    private CQLQualityDataSetDTO createValueSet(String codeIdentifier) {
        CQLQualityDataSetDTO cqlQualityDataSetDTO = new CQLQualityDataSetDTO();
        cqlQualityDataSetDTO.setId(codeIdentifier);

        cqlQualityDataSetDTO.setCodeListName(codeIdentifier);

        return cqlQualityDataSetDTO;
    }
}
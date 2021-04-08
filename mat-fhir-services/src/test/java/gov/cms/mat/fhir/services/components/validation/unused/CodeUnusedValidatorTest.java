package gov.cms.mat.fhir.services.components.validation.unused;

import mat.model.cql.CQLCode;
import mat.model.cql.CQLDefinition;
import mat.model.cql.CQLFunctions;
import mat.model.cql.CQLModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeUnusedValidatorTest {
    private CQLModel cqlModel;
    private CodeUnusedValidator validator;

    private CQLCode used;
    private CQLCode unUsed;

    @BeforeEach
    void setUp() {
        cqlModel = new CQLModel();
        validator = new CodeUnusedValidator(cqlModel);

        used = createIncludeLibrary("used");
        unUsed = createIncludeLibrary("unused");
    }

    @Test
    void findUnusedNoEntries() {
        assertTrue(validator.findUnused().isEmpty());
    }

    @Test
    void findUnusedInFunctions() {
        cqlModel.setCodeList(List.of(used, unUsed));

        CQLFunctions cqlFunction = new CQLFunctions();
        cqlFunction.setFunctionLogic("I am \"used\" up");
        cqlModel.setCqlFunctions(List.of(cqlFunction));

        List<CQLCode> unused = validator.findUnused();
        assertEquals(1, unused.size());
        assertEquals(unUsed, unused.get(0));
    }

    @Test
    void findUnusedInDefines() {
        cqlModel.setCodeList(List.of(used, unUsed));


        CQLDefinition cqlDefinition = new CQLDefinition();
        cqlDefinition.setDefinitionLogic("I am \"used\" up");
        cqlModel.setDefinitionList(List.of(cqlDefinition));

        List<CQLCode> unused = validator.findUnused();
        assertEquals(1, unused.size());
        assertEquals(unUsed, unused.get(0));
    }

    private CQLCode createIncludeLibrary(String codeName) {
        CQLCode cqlCode = new CQLCode();
        cqlCode.setCodeName(codeName);
        return cqlCode;
    }
}
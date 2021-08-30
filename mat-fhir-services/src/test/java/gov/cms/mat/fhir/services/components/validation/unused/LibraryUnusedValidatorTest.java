package gov.cms.mat.fhir.services.components.validation.unused;

import mat.model.cql.CQLDefinition;
import mat.model.cql.CQLFunctions;
import mat.model.cql.CQLIncludeLibrary;
import mat.model.cql.CQLModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryUnusedValidatorTest {
    private CQLModel cqlModel;
    private LibraryUnusedValidator validator;
    CQLIncludeLibrary used;
    CQLIncludeLibrary unUsed;

    @BeforeEach
    void setUp() {
        cqlModel = new CQLModel();
        validator = new LibraryUnusedValidator(cqlModel);

        used = createIncludeLibrary("used");
        unUsed = createIncludeLibrary("unused");
    }

    @Test
    void findUnusedNoEntries() {
        assertTrue(validator.findUnused().isEmpty());
    }

    @Test
    void findUnusedInFunctions() {
        cqlModel.setCqlIncludeLibrarys(List.of(used, unUsed));

        CQLFunctions cqlFunction = new CQLFunctions();
        cqlFunction.setFunctionLogic("I am used.doStuff()");
        cqlModel.setCqlFunctions(List.of(cqlFunction));

        List<CQLIncludeLibrary> unused = validator.findUnused();
        assertEquals(1, unused.size());
        assertEquals(unUsed, unused.get(0));
    }

    @Test
    void findUnusedInDefines() {
        cqlModel.setCqlIncludeLibrarys(List.of(used, unUsed));

        CQLDefinition cqlDefinition = new CQLDefinition();
        cqlDefinition.setDefinitionLogic("I used.doStuff()");
        cqlModel.setDefinitionList(List.of(cqlDefinition));

        List<CQLIncludeLibrary> unused = validator.findUnused();
        assertEquals(1, unused.size());
        assertEquals(unUsed, unused.get(0));
    }

    private CQLIncludeLibrary createIncludeLibrary(String alias) {
        CQLIncludeLibrary cqlIncludeLibrary = new CQLIncludeLibrary();
        cqlIncludeLibrary.setAliasName(alias);
        cqlIncludeLibrary.setCqlLibraryName(alias);
        return cqlIncludeLibrary;
    }
}
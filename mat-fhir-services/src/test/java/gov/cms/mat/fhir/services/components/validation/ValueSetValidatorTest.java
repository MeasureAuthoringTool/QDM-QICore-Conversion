package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import mat.model.cql.CQLQualityDataSetDTO;
import mat.shared.CQLError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueSetValidatorTest implements ResourceFileUtil {
    private static final String TOKEN = "token";
    private static final String API_KEY = "api_key";
    private static final long TIMEOUT = 0;
    private String cql = "";

    private List<CQLQualityDataSetDTO> valueSetList;

    @Mock
    private VsacValueSetValidator vsacValueSetValidator;

    @InjectMocks
    private ValueSetValidator valueSetValidator;

    @BeforeEach
    void setUp() {
        cql = getStringFromResource("/test-cql/AdultOutpatientEncounters_FHIR4-1.1.000.cql");
        valueSetList = new ArrayList<>();
    }

    @Test
    void validateNoFailingValueSets() throws ExecutionException, InterruptedException {
        when(vsacValueSetValidator
                .validate(TIMEOUT, valueSetList, TOKEN, API_KEY))
                .thenReturn(Collections.emptyList());

        CompletableFuture<List<LibraryErrors>> future = valueSetValidator.validate(TIMEOUT, valueSetList, cql, TOKEN, API_KEY);

        assertTrue(future.get().isEmpty());
    }

    @Test
    void validate() throws ExecutionException, InterruptedException {
        when(vsacValueSetValidator
                .validate(TIMEOUT, valueSetList, TOKEN, API_KEY))
                .thenReturn(List.of(createCQLQualityDataSetDTO()));

        CompletableFuture<List<LibraryErrors>> future = valueSetValidator.validate(TIMEOUT, valueSetList, cql, TOKEN, API_KEY);

        List<LibraryErrors> libraryErrors = future.get();

        assertEquals(1, libraryErrors.size());

        LibraryErrors libraryError = libraryErrors.get(0);

        assertEquals("AdultOutpatientEncounters_FHIR4", libraryError.getName()); // from cql
        assertEquals("1.1.000", libraryError.getVersion()); // from cql

        assertEquals(1, libraryError.getErrors().size());

        CQLError cqlError = libraryError.getErrors().get(0);

        assertEquals("We need a much bigger boat", cqlError.getErrorMessage());
        assertEquals("Error", cqlError.getSeverity());
    }

    CQLQualityDataSetDTO createCQLQualityDataSetDTO() {
        CQLQualityDataSetDTO cqlQualityDataSetDTO = new CQLQualityDataSetDTO();

        cqlQualityDataSetDTO.setOid("2.16.840.1.113883.3.464.1003.101.12.1016");
        cqlQualityDataSetDTO.setErrorMessage("We need a much bigger boat");

        return cqlQualityDataSetDTO;
    }
}
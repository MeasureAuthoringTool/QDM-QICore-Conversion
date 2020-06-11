package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.CqlHelper;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import mat.model.cql.CQLModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueSetValidatorTest implements CqlHelper {
    private static final String TOKEN = "token";

    @Mock
    private VsacValueSetValidator vsacValueSetValidator;
    @InjectMocks
    private ValueSetValidator valueSetValidator;

    private CQLModel cqlModel;
    private String cql;

    @BeforeEach
    void setUp() {
        cqlModel = loadMatXml("/test-cql/convert-1-mat.xml");
        cql = getStringFromResource("/test-cql/convert-1.cql");
    }


    @Test
    void validateNoFailingValueSets() throws ExecutionException, InterruptedException {
        when(vsacValueSetValidator.validate(cqlModel.getValueSetList(), TOKEN)).thenReturn(Collections.emptyList());

        CompletableFuture<List<LibraryErrors>> completableFuture =
                valueSetValidator.validate(cqlModel.getValueSetList(), cql, TOKEN);

        List<LibraryErrors> libraryErrors = completableFuture.get();

        assertTrue(libraryErrors.isEmpty());
    }

    @Test
    void validateFailingValueSets() throws ExecutionException, InterruptedException {
        when(vsacValueSetValidator.validate(cqlModel.getValueSetList(), TOKEN))
                .thenReturn(List.of(cqlModel.getValueSetList().get(0), cqlModel.getValueSetList().get(1)));

        CompletableFuture<List<LibraryErrors>> completableFuture =
                valueSetValidator.validate(cqlModel.getValueSetList(), cql, TOKEN);

        List<LibraryErrors> libraryErrors = completableFuture.get();

        assertEquals(1, libraryErrors.size());
        assertEquals(2, libraryErrors.get(0).getErrors().size());
        assertEquals(13, libraryErrors.get(0).getErrors().get(0).getErrorInLine());
        assertEquals(14, libraryErrors.get(0).getErrors().get(1).getErrorInLine());
    }
}
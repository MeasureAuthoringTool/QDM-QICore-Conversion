package gov.cms.mat.fhir.services.rest;


import gov.cms.mat.fhir.rest.cql.ConversionType;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CqlLibraryVerifyControllerTest {
    private static final String RESULT_EXPECTED = "result";

    @Mock
    private CQLLibraryTranslationService cqlLibraryTranslationService;
    @InjectMocks
    private CqlLibraryVerifyController cqlLibraryVerifyController;

    @Test
    void translateAll() {
        when(cqlLibraryTranslationService.processAll()).thenReturn(RESULT_EXPECTED);

        assertEquals(RESULT_EXPECTED, cqlLibraryVerifyController.translateAll());

        verify(cqlLibraryTranslationService).processAll();
    }

    @Test
    void translateOne() {
        String measureId = "measureId";

        when(cqlLibraryTranslationService.processOne(measureId, ConversionType.CONVERSION)).thenReturn(RESULT_EXPECTED);

        assertEquals(RESULT_EXPECTED, cqlLibraryVerifyController.translateOne(measureId));

        verify(cqlLibraryTranslationService).processOne(measureId, ConversionType.CONVERSION);
    }
}
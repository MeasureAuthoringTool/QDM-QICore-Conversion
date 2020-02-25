package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CqlLibraryConverterTest implements ResourceFileUtil {
    @InjectMocks
    CqlLibraryConverter cqlLibraryConverter;
    @Mock
    private QdmQiCoreDataService qdmQiCoreDataService;

    @Test
    void convert() {
        when(qdmQiCoreDataService.findAllFilteredByMatDataTypeDescription(any())).thenReturn(Collections.emptyList());

        String cql = getStringFromResource("/fhir/Hospice_FHIR4-1.0.000.cql");

        String s = cqlLibraryConverter.convert(cql);

        assertTrue(s.contains("library Hospice_FHIR4_FHIR4 version '1.0.000'"));

        verify(qdmQiCoreDataService).findAllFilteredByMatDataTypeDescription(any());
    }
}
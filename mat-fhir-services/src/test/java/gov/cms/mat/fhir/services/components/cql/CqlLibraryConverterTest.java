package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CqlLibraryConverterTest implements ResourceFileUtil {
    @InjectMocks
    CqlLibraryConverter cqlLibraryConverter;
    @Mock
    private QdmQiCoreDataService qdmQiCoreDataService;

    @Test
    void convert() {
        String cql = getStringFromResource("/fhir/Hospice_FHIR4-1.0.000.cql");

        String s = cqlLibraryConverter.convert(cql);

        assertTrue(s.contains("library Hospice_FHIR4_FHIR4 version '1.0.000'"));
    }
}
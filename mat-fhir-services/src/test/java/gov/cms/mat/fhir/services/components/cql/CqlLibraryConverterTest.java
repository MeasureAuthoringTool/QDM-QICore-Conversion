package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static gov.cms.mat.fhir.services.cql.QdmCqlToFhirCqlConverter.STD_FHIR_LIBS;
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

        String converted = cqlLibraryConverter.convert(cql);

        assertTrue(converted.contains("library Hospice_FHIR4_FHIR4 version '1.0.000'"));
    }

    @Test
    void convert_VerifySrdLibs() {
        String cql = getStringFromResource("/test_std_includes.cql");

        String converted = cqlLibraryConverter.convert(cql);

        assertTrue(converted.contains(STD_FHIR_LIBS));
        assertTrue(converted.contains("define \"SDE Ethnicity\""));
        assertTrue(converted.contains("define \"SDE Sex\""));
    }
}
package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CqlLibraryConverterTest implements ResourceFileUtil {

    CqlLibraryConverter cqlLibraryConverter;

    private QdmQiCoreDataService qdmQiCoreDataService;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        qdmQiCoreDataService = new QdmQiCoreDataService(restTemplate);
        ReflectionTestUtils.setField(qdmQiCoreDataService, "baseURL", "http://localhost:9090");

        cqlLibraryConverter = new CqlLibraryConverter(qdmQiCoreDataService);
    }


    @Test
    void convert() {
        String cql = getStringFromResource("/fhir/Hospice_FHIR4-1.0.000.cql");

        String converted = cqlLibraryConverter.convert(cql);

        assertTrue(converted.contains("library Hospice_FHIR4_FHIR4 version '1.0.000'"));
    }

    @Test
    void convert_VerifyStandardLibs() {
        String cql = getStringFromResource("/test_std_includes.cql");

        String converted = cqlLibraryConverter.convert(cql);

        //assertTrue(converted.contains(STD_FHIR_LIBS));
        assertTrue(converted.contains("define \"SDE Ethnicity\""));
        assertTrue(converted.contains("define \"SDE Sex\""));
    }


    @Test
    void convert_Called() {
        String matGlobalCommonFunctions = "include MATGlobalCommonFunctions version '4.1.000' called Global";

        String cql = getStringFromResource("/called.cql");
        assertTrue(cql.contains(matGlobalCommonFunctions));

        String converted = cqlLibraryConverter.convert(cql);

        System.out.println(converted);

        assertTrue(converted.contains("include FHIRHelpers version '4.0.0' called FHIRHelpers"));
        assertTrue(converted.contains("include SupplementalDataElements_FHIR4 version '1.0.0' called SDE"));
        assertTrue(converted.contains("include MATGlobalCommonFunctions_FHIR4 version '4.0.000' called Global"));

        assertFalse(converted.contains(matGlobalCommonFunctions));
    }
}
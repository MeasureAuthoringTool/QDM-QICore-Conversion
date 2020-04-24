package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.config.CodeSystemLookup;
import gov.cms.mat.fhir.services.config.ConversionLibraryLookup;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CodeSystemConversionDataService;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@ExtendWith(MockitoExtension.class)
class CqlLibraryConverterTest implements ResourceFileUtil {

    private CqlLibraryConverter cqlLibraryConverter;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        CodeSystemConversionDataService codeSystemConversionDataService = new CodeSystemConversionDataService(restTemplate);
        ReflectionTestUtils.setField(codeSystemConversionDataService,
                "url",
                "https://spreadsheets.google.com/feeds/list/15YvJbG3LsyqqN4ZIgRd88fgScbE95eK6fUilwHRw0Z0/od6/public/values?alt=json");
        codeSystemConversionDataService.postConstruct();


        CodeSystemLookup codeSystemLookup = new CodeSystemLookup();
        codeSystemLookup.setMap(createCodeSystemLookUpMap());

        ConversionLibraryLookup conversionLibraryLookup = new ConversionLibraryLookup();
        conversionLibraryLookup.setMap(createConvertedLibLookUpMap());


        QdmQiCoreDataService qdmQiCoreDataService = new QdmQiCoreDataService(restTemplate);
        ReflectionTestUtils.setField(qdmQiCoreDataService, "baseURL", "http://localhost:9090");

        HapiFhirServer hapiFhirServer = new HapiFhirServer();
        ReflectionTestUtils.setField(hapiFhirServer, "baseURL", "http://localhost:6060/hapi-fhir-jpaserver/fhir/");
        hapiFhirServer.setUp();

        cqlLibraryConverter = new CqlLibraryConverter(qdmQiCoreDataService,
                conversionLibraryLookup,
                codeSystemConversionDataService,
                hapiFhirServer);
    }

    private Map<String, String> createConvertedLibLookUpMap() {
        return Map.of(
                "FHIRHelpers", "4.0.001",
                "AdultOutpatientEncounters", "2.0.000",
                "AdvancedIllnessandFrailtyExclusion", "5.0.000",
                "Hospice", "2.0.000",
                "MATGlobalCommonFunctions", "5.0.000",
                "SupplementalDataElements", "2.0.000",
                "TJCOverall", "5.0.000",
                "VTEICU", "4.0.000");
    }

    private Map<String, String> createCodeSystemLookUpMap() {
        return Map.of(
                "LOINC", "http://loinc.org",
                "SNOMEDCT", "http://snomed.info/sct/731000124108",
                "Diagnosis Role", "http://terminology.hl7.org/CodeSystem/diagnosis-role",
                "RoleCode", "http://hl7.org/fhir/v3/RoleCode");
    }

    @Test
    void convert() {
        String cql = getStringFromResource("/fhir/Hospice_FHIR4-1.0.000.cql");

        String converted = cqlLibraryConverter.convert(cql);

        assertTrue(converted.contains("library Hospice_FHIR4 version '1.0.000'"));
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

        assertTrue(converted.contains("include FHIRHelpers version '4.0.001' called FHIRHelpers"));
        assertTrue(converted.contains("include SupplementalDataElements_FHIR4 version '2.0.000' called SDE"));
        assertTrue(converted.contains("include MATGlobalCommonFunctions_FHIR4 version '5.0.000' called Global"));

        assertFalse(converted.contains(matGlobalCommonFunctions));
    }

    @Test
    void convert_TestIncludeProcessing() {
        String matGlobalCommonFunctions = "include MATGlobalCommonFunctions version '4.0.000' called Global";

        String cql = getStringFromResource("/test_include_processing.cql");
        assertTrue(cql.contains(matGlobalCommonFunctions));

        assertTrue(cql.contains("include MATGlobalCommonFunctions version '4.0.000' called Global"));
        assertTrue(cql.contains("include SupplementalDataElements version '1.0.0' called SDE"));
        assertTrue(cql.contains("include AdultOutpatientEncounters version '1.2.345' called ADP"));
        assertTrue(cql.contains("include AdvancedIllnessandFrailtyExclusion version '5.6.789' called AVI"));
        assertTrue(cql.contains("include Hospice version '0.1.234' called HSP"));
        assertTrue(cql.contains("include TJCOverall version '5.6.789' called TJC"));
        assertTrue(cql.contains("include VTEICU version '5.6.789' called VTE"));

        String converted = cqlLibraryConverter.convert(cql);

        System.out.println(converted);


        assertTrue(converted.contains("include VTEICU_FHIR4 version '4.0.000' called VTE"));


        assertTrue(converted.contains("include FHIRHelpers version '4.0.001' called FHIRHelpers"));
        assertTrue(converted.contains("include SupplementalDataElements_FHIR4 version '2.0.000' called SDE"));
        assertTrue(converted.contains("include MATGlobalCommonFunctions_FHIR4 version '5.0.000' called Global"));

        assertFalse(converted.contains(matGlobalCommonFunctions));
    }
}
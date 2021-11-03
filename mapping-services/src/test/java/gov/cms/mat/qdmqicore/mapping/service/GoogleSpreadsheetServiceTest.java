package gov.cms.mat.qdmqicore.mapping.service;

import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionAttributes;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionDataTypes;
import gov.cms.mat.fhir.rest.dto.spreadsheet.DataType;
import gov.cms.mat.fhir.rest.dto.spreadsheet.FhirLightBoxDatatypeAttributeAssociations;
import gov.cms.mat.fhir.rest.dto.spreadsheet.MatAttribute;
import gov.cms.mat.fhir.rest.dto.spreadsheet.QdmToQicoreMapping;
import gov.cms.mat.fhir.rest.dto.spreadsheet.RequiredMeasureField;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ResourceDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GoogleSpreadsheetServiceTest {

    @Autowired
    private MappingService googleSpreadsheetService;

    @Test
    void getMatAttributes() throws IOException {
        List<MatAttribute> result = googleSpreadsheetService.getMatAttributes();
        assertAll(
                "get Mat Attributes Successfully",
                () -> assertEquals(5, result.size()),
                () -> assertEquals("Adverse Event", result.get(0).getDataTypeDescription()),
                () -> assertEquals("severity", result.get(0).getMatAttributeName()),
                () -> assertEquals("AdverseEvent.severity", result.get(0).getFhirQicoreMapping()),
                () -> assertEquals("AdverseEvent", result.get(0).getFhirResource()),
                () -> assertEquals("CodeableConcept", result.get(0).getFhirType()),
                () -> assertEquals("severity", result.get(0).getFhirElement()),
                () -> assertEquals(3, result.get(0).getDropDown().size())
        );
    }

    @Test
    void getQdmToQicoreMapping() throws IOException {
        List<QdmToQicoreMapping> result = googleSpreadsheetService.getQdmToQicoreMapping();
        assertAll(
                "get Qdm to Qicore mapping successfully",
                () -> assertEquals(5, result.size()),
                () -> assertEquals("Adverse Event", result.get(0).getTitle()),
                () -> assertNull(result.get(0).getMatDataType()),
                () -> assertEquals("code", result.get(1).getMatAttributeType()),
                () -> assertEquals("AdverseEvent.actuality", result.get(0).getFhirQICoreMapping()),
                () -> assertEquals("code\n", result.get(0).getType()),
                () -> assertEquals("1..1", result.get(0).getCardinality())
        );
    }

    @Test
    void getDataTypes() throws IOException {
        List<DataType> result = googleSpreadsheetService.getDataTypes();
        assertAll(
                "get data types successfully",
                () -> assertEquals(5, result.size()),
                () -> assertEquals("Boolean", result.get(0).getDataType()),
                () -> assertEquals("True\nFalse", result.get(0).getValidValues()),
                () -> assertEquals("true|false", result.get(0).getRegex()),
                () -> assertEquals("string", result.get(0).getType())
        );
    }

    @Test
    void getRequiredMeasureFields() throws IOException {
        List<RequiredMeasureField> result = googleSpreadsheetService.getRequiredMeasureFields();
        assertAll(
                "get required measure fields successfully",
                () -> assertEquals(8, result.size()),
                () -> assertEquals("id", result.get(0).getField()),
                () -> assertEquals("string", result.get(0).getType())
        );
    }

    @Test
    void getResourceDefinitions() throws IOException {
        List<ResourceDefinition> result = googleSpreadsheetService.getResourceDefinitions();
        assertAll(
                "get resource definitions successfully",
                () -> assertEquals(4, result.size()),
                () -> assertEquals("Account", result.get(0).getElementId()),
                () -> assertEquals("0..*", result.get(0).getCardinality()),
                () -> assertEquals("DomainResource\n", result.get(0).getType()),
                () -> assertEquals("", result.get(0).getIsModifier()),
                () -> assertEquals("", result.get(0).getIsSummary())
        );
    }

    @Test
    void getConversionDataTypes() throws IOException {
        List<ConversionDataTypes> result = googleSpreadsheetService.getConversionDataTypes();
        assertAll(
                "get conversion data types successfully",
                () -> assertEquals(5, result.size()),
                () -> assertEquals("Adverse Event", result.get(0).getQdmType()),
                () -> assertEquals("AdverseEvent", result.get(0).getFhirType()),
                () -> assertEquals("", result.get(0).getWhereAdjustment()),
                () -> assertEquals("", result.get(0).getComment())
        );
    }

    @Test
    void getConversionAttributes() throws IOException {
        List<ConversionAttributes> result = googleSpreadsheetService.getConversionAttributes();
        assertAll(
                "get conversion attributes successfully",
                () -> assertEquals(5, result.size()),
                () -> assertEquals("Adverse Event", result.get(0).getQdmType()),
                () -> assertEquals("authorDatetime", result.get(0).getQdmAttribute()),
                () -> assertEquals("AdverseEvent", result.get(0).getFhirType()),
                () -> assertEquals("recordedDate", result.get(0).getFhirAttribute()),
                () -> assertEquals("", result.get(0).getComment())
        );
    }

    @Test
    void getFhirLightBoxDatatypeAttributeAssociation() throws IOException {
        List<FhirLightBoxDatatypeAttributeAssociations> result = googleSpreadsheetService.getFhirLightBoxDatatypeAttributeAssociation();
        assertAll(
                "get fhir lightbox datatype attribute associations successfully",
                () -> assertEquals(6, result.size()),
                () -> assertEquals("AdverseEvent", result.get(0).getDatatype()),
                () -> assertEquals("actuality", result.get(0).getAttribute()),
                () -> assertEquals("FHIR.AdverseEventActuality", result.get(0).getAttributeType()),
                () -> assertTrue(result.get(0).getHasBinding())
        );
    }

    @Test
    void getFhirLightboxDataTypesForFunctionArgs() throws IOException {
        List<String> result = googleSpreadsheetService.getFhirLightboxDataTypesForFunctionArgs();
        assertEquals(10, result.size());
    }

    @Test
    void getPopulationBasisValidValues() throws IOException {
        List<String> result = googleSpreadsheetService.getPopulationBasisValidValues();
        assertEquals(10, result.size());
    }

    @Test
    void getCodeSystemEntries() throws IOException {
        List<CodeSystemEntry> result = googleSpreadsheetService.getCodeSystemEntries();
        assertAll(
                "get code systems successfully",
                () -> assertEquals(5, result.size()),
                () -> assertEquals("urn:oid:2.16.840.1.113883.5.4", result.get(0).getOid()),
                () -> assertEquals("http://terminology.hl7.org/CodeSystem/v3-ActCode", result.get(0).getUrl()),
                () -> assertEquals("ActCode", result.get(0).getName())
        );
    }
}
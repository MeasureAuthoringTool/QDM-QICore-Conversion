package gov.cms.mat.fhir.services.components.conversion;

import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionAttributes;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionDataTypes;
import gov.cms.mat.fhir.services.cql.parser.MappingSpreadsheetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionDataComponentTest {
    private static final String QDM_TYPE = '"' + "qdm_type" + '"';
    private static final String QDM_ATTRIBUTE = "qdm_attribute_qdm_type";
    private static final String FHIR_TYPE = '"' + "fhir_type" + '"';
    @Mock
    private MappingSpreadsheetService mappingSpreadsheetService;

    @InjectMocks
    private ConversionDataComponent conversionDataComponent;

    @Test
    void findFhirTypeEmptyList() {
        when(mappingSpreadsheetService.fetchConversionDataTypes()).thenReturn(Collections.emptyList());

        assertTrue(conversionDataComponent.findFhirType(QDM_TYPE).isEmpty());
    }

    @Test
    void findFhirTypeEmptyWrongType() {
        ConversionDataTypes conversionDataTypesFhir = createConversionDataType("fhir");
        ConversionDataTypes conversionDataTypesQuick = createConversionDataType("quick");

        when(mappingSpreadsheetService.fetchConversionDataTypes())
                .thenReturn(List.of(conversionDataTypesFhir, conversionDataTypesQuick));

        assertTrue(conversionDataComponent.findFhirType(QDM_TYPE).isEmpty());
    }

    @Test
    void findFhirType() {
        ConversionDataTypes conversionDataTypesQdm = createConversionDataType("qdm_type");

        ConversionDataTypes conversionDataTypesJava = createConversionDataType("java");

        when(mappingSpreadsheetService.fetchConversionDataTypes())
                .thenReturn(List.of(conversionDataTypesQdm, conversionDataTypesJava));
        Optional<ConversionDataTypeResult> optional = conversionDataComponent.findFhirType(QDM_TYPE);
        assertTrue(optional.isPresent());

        ConversionDataTypeResult result = optional.get();
        assertEquals("fhir_type", result.getType());
        assertEquals("comment_qdm_type", result.getComment());
        assertEquals("where_qdm_type", result.getWhereAdjustment());
    }

    @Test
    void findFhirAttributeEmptyList() {
        when(mappingSpreadsheetService.fetchConversionAttributes()).thenReturn(Collections.emptyList());

        assertTrue(conversionDataComponent.findFhirAttribute(QDM_TYPE, QDM_ATTRIBUTE, FHIR_TYPE).isEmpty());

    }

    @Test
    void findFhirAttributeWrongFhirType() {
        ConversionAttributes conversionAttributesFhir = createConversionAttribute("fhir");
        ConversionAttributes conversionAttributesQuick = createConversionAttribute("quick");

        when(mappingSpreadsheetService.fetchConversionAttributes())
                .thenReturn(List.of(conversionAttributesFhir, conversionAttributesQuick));

        assertTrue(conversionDataComponent.findFhirAttribute(QDM_TYPE, QDM_ATTRIBUTE, FHIR_TYPE).isEmpty());
    }

    @Test
    void findFhirAttribute() {
        ConversionAttributes conversionAttributesFhir = createConversionAttribute("qdm_type");
        ConversionAttributes conversionAttributesJava = createConversionAttribute("java");

        when(mappingSpreadsheetService.fetchConversionAttributes())
                .thenReturn(List.of(conversionAttributesFhir, conversionAttributesJava));

        Optional<ConversionFhirAttributeResult> optional = conversionDataComponent.findFhirAttribute(QDM_TYPE, QDM_ATTRIBUTE, FHIR_TYPE);
        assertTrue(optional.isPresent());

        ConversionFhirAttributeResult result = optional.get();

        assertEquals("fhir_attribute_type", result.getType());
        assertEquals("comment_qdm_type", result.getComment());
        assertEquals("fhir_attribute_type", result.getFhirAttribute());

    }

    @Test
    void findCommentForMissingType() {
        ConversionAttributes conversionAttributesComment = createConversionAttribute("comment");
        conversionAttributesComment.setFhirType("ALL");

        ConversionAttributes conversionAttributesJava = createConversionAttribute("java");

        when(mappingSpreadsheetService.fetchConversionAttributes())
                .thenReturn(List.of(conversionAttributesComment, conversionAttributesJava));

        Optional<String> optional = conversionDataComponent.findCommentForMissingType("qdm_attribute_comment");
        assertTrue(optional.isPresent());

        String result = optional.get();

        assertEquals("comment_comment", result);
    }

    private ConversionDataTypes createConversionDataType(String type) {
        ConversionDataTypes conversionDataTypesFhir = new ConversionDataTypes();
        conversionDataTypesFhir.setQdmType(type);

        String fhirType = type.replace("qdm_", "fhir_");
        conversionDataTypesFhir.setFhirType(fhirType);
        conversionDataTypesFhir.setWhereAdjustment("where_" + type);
        conversionDataTypesFhir.setComment("comment_" + type);

        return conversionDataTypesFhir;
    }

    private ConversionAttributes createConversionAttribute(String type) {
        ConversionAttributes conversionAttributes = new ConversionAttributes();
        conversionAttributes.setQdmType(type);

        conversionAttributes.setQdmAttribute("qdm_attribute_" + type);

        String fhirType = type.replace("qdm_", "fhir_");
        conversionAttributes.setFhirType(fhirType);

        String fhirAttribute = type.replace("qdm_", "fhir_attribute_");
        conversionAttributes.setFhirAttribute(fhirAttribute);


        conversionAttributes.setComment("comment_" + type);

        return conversionAttributes;
    }
}
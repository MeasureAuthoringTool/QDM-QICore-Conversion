package gov.cms.mat.fhir.services.components.conversion;

import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionAttributes;
import gov.cms.mat.fhir.services.cql.parser.MappingSpreadsheetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class ConversionDataComponent {
    private final MappingSpreadsheetService mappingSpreadsheetService;

    public ConversionDataComponent(MappingSpreadsheetService mappingSpreadsheetService) {
        this.mappingSpreadsheetService = mappingSpreadsheetService;
    }

    public boolean isFhirAttributeMapped(String qdmType,
                                        String fhirAttribute,
                                        String fhirType) {
        return mappingSpreadsheetService.fetchConversionAttributes().stream()
                .anyMatch(c -> isFhirAttributeMatchByFhirAttribute(c, qdmType,fhirAttribute, fhirType));
    }

    public Optional<ConversionDataTypeResult> findFhirType(String qdmType) {
        return mappingSpreadsheetService.fetchConversionDataTypes().stream()
                .filter(c -> Objects.equals('"' + c.getQdmType() + '"', qdmType))
                .map(c -> buildFhirDataTypeResult(c.getFhirType(), c.getComment(), c.getWhereAdjustment()))
                .findFirst();
    }

    public Optional<ConversionFhirAttributeResult> findFhirAttribute(String qdmType,
                                                                     String qdmAttribute,
                                                                     String fhirType) {
        return mappingSpreadsheetService.fetchConversionAttributes().stream()
                .peek(c -> log.debug("c= {}", c))
                .filter(c -> isFhirAttributeMatchByQdmAttribute(c, qdmType, qdmAttribute, fhirType))
                .map(c -> buildFhirAttributeResult(c.getFhirAttribute(), c.getComment(), c.getFhirAttribute()))
                .findFirst();
    }

    public Optional<String> findCommentForMissingType(String qdmAttribute) {
        return mappingSpreadsheetService.fetchConversionAttributes().stream()
                .filter(c -> isCommentRow(qdmAttribute, c))
                .map(ConversionAttributes::getComment)
                .findFirst();
    }

    private boolean isCommentRow(String qdmAttribute, ConversionAttributes attributes) {


        return attributes.getQdmAttribute().equals(qdmAttribute) &&
                attributes.getFhirType().equals("ALL") &&
                StringUtils.isNotBlank(attributes.getComment());
    }


    private boolean isFhirAttributeMatchByQdmAttribute(ConversionAttributes attributes,
                                                       String qdmType,
                                                       String qdmAttribute,
                                                       String fhirType) {
        return attributes.getQdmType().equals(qdmType.replace("\"", "")) &&
                attributes.getQdmAttribute().equals(qdmAttribute) &&
                attributes.getFhirType().equals(fhirType.replace("\"", ""));

    }

    private boolean isFhirAttributeMatchByFhirAttribute(ConversionAttributes attributes,
                                                        String qdmType,
                                                        String fhirAttribute,
                                                        String fhirType) {
        return attributes.getQdmType().equals(qdmType.replace("\"", "")) &&
                attributes.getFhirAttribute().equals(fhirAttribute) &&
                attributes.getFhirType().equals(fhirType.replace("\"", ""));

    }

    private ConversionFhirAttributeResult buildFhirAttributeResult(String type, String comment, String fhirAttribute) {
        return ConversionFhirAttributeResult.builder()
                .type(type)
                .comment(comment)
                .fhirAttribute(fhirAttribute)
                .build();
    }

    private ConversionDataTypeResult buildFhirDataTypeResult(String type, String comment, String whereAdjustment) {
        return ConversionDataTypeResult.builder()
                .type(type)
                .comment(comment)
                .whereAdjustment(whereAdjustment)
                .build();
    }
}

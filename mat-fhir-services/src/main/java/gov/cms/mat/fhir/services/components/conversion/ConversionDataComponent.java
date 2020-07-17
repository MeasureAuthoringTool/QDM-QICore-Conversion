package gov.cms.mat.fhir.services.components.conversion;

import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionAttributes;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionDataTypes;
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

    public Optional<String> findFhirType(String qdmType) {
        return mappingSpreadsheetService.fetchConversionDataTypes().stream()
                .filter(c -> Objects.equals('"' + c.getQdmType() + '"', qdmType))
                .map(ConversionDataTypes::getFhirType)
                .findFirst();
    }

    public Optional<String> findFhirAttribute(String qdmType, String qdmAttribute, String fhirType) {
        return mappingSpreadsheetService.fetchConversionAttributes().stream()
                .peek(c -> log.debug("" + c))
                .filter(c -> isFhirAttributeMatch(c, qdmType, qdmAttribute, fhirType))
                .map(ConversionAttributes::getFhirAttribute)
                .findFirst();
    }

    private boolean isFhirAttributeMatch(ConversionAttributes attributes, String qdmType, String qdmAttribute, String fhirType) {

        String qdmTypeQuoted = '"' + attributes.getQdmType() + '"';
        String fhirTypeQuoted = '"' + attributes.getFhirType() + '"';

        return qdmTypeQuoted.equals(qdmType) &&
                attributes.getQdmAttribute().equals(qdmAttribute) &&
                fhirTypeQuoted.equals(fhirType);

    }
}

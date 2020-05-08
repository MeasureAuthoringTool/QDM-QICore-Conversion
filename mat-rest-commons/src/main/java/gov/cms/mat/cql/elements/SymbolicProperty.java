package gov.cms.mat.cql.elements;

import gov.cms.mat.fhir.rest.dto.spreadsheet.QdmToFhirMappingHelper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Builder
@Getter
@ToString
@Slf4j
public class SymbolicProperty {
    @Setter
    private String matDataTypeDescription;
    private final String symbolic;
    @Setter
    private Set<SymbolicAttributeProperty> attributePropertySet;
    @Getter
    @Setter
    private QdmToFhirMappingHelper helper;


}

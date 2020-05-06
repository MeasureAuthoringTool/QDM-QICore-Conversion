package gov.cms.mat.cql.elements;

import gov.cms.mat.cql.CqlNegations;
import gov.cms.mat.fhir.rest.dto.spreadsheet.MatAttribute;
import gov.cms.mat.fhir.rest.dto.spreadsheet.QdmToFhirMappingHelper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

@Builder
@Getter
@ToString
@Slf4j
public class SymbolicProperty {
    @Setter
    private String matDataTypeDescription;
    private String symbolic;
    @Setter
    private Set<SymbolicAttributeProperty> attributePropertySet;
    @Getter
    @Setter
    private QdmToFhirMappingHelper helper;


}

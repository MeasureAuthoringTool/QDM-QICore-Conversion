package gov.cms.mat.cql.elements;

import gov.cms.mat.cql.CqlNegations;
import gov.cms.mat.cql.exceptions.QdmMappingException;
import gov.cms.mat.fhir.rest.dto.ConversionMapping;
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
    private List<ConversionMapping> conversionMappings;
    private String matDataTypeDescription;
    private String symbolic;
    @Setter
    private Set<SymbolicAttributeProperty> attributePropertySet;

    public String findFhirResource() {
        if (CollectionUtils.isEmpty(conversionMappings)) {

            if (!filterNegations()) {
                log.info("Ignoring negations: {}", matDataTypeDescription);
                return matDataTypeDescription;
            }
            log.warn("QDM Spreadsheet error! Could not find fhir mapping for: {}", matDataTypeDescription);
            return matDataTypeDescription;
        } else {
            return conversionMappings.get(0).getFhirResource(); // should I check if all the same
        }
    }

    private boolean filterNegations() {
        return StringUtils.indexOfAny(matDataTypeDescription, CqlNegations.getNegations()) < 0;
    }
}

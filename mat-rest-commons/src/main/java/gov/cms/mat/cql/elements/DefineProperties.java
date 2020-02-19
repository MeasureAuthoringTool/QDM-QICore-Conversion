package gov.cms.mat.cql.elements;

import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

@Builder
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class DefineProperties extends BaseProperties {
    String defineData;
    String line;
    List<SymbolicProperty> symbolicProperties;

    @Override
    public void setToFhir() {
        if (line.contains("define \"SDE ")) {
            log.debug("Define is SDE");
        } else {
            if (CollectionUtils.isEmpty(symbolicProperties)) {
                log.debug("No Symbolics");
            } else {
                symbolicProperties.forEach(this::process);
            }
        }
    }

    private void process(SymbolicProperty symbolicProperty) {
        defineData = StringUtils.replace(defineData, '"' + symbolicProperty.getMatDataTypeDescription() + '"',
                '"' + symbolicProperty.findFhirResource() + '"');

        if (CollectionUtils.isNotEmpty(symbolicProperty.getAttributePropertySet())) {
            symbolicProperty.getAttributePropertySet()
                    .forEach(a -> processAttribute(a, symbolicProperty));
        }

    }

    private void processAttribute(SymbolicAttributeProperty symbolicAttributeProperty,
                                  SymbolicProperty symbolicProperty) {

        String matAttributeName = symbolicAttributeProperty.getMatAttributeName();

        Optional<ConversionMapping> optional = symbolicProperty.getConversionMappings().stream()
                .filter(c -> c.getMatAttributeName().equals(matAttributeName))
                .findFirst();

        if (optional.isPresent()) {
            defineData =
                    defineData.replace(symbolicAttributeProperty.getUsing(),
                            symbolicAttributeProperty.getSymbolicName() + '.' + optional.get().getFhirElement());
        } else {
            log.warn("Cannot find with matDataTypeDescription: {} - matAttributeName: {}",
                    symbolicProperty.getMatDataTypeDescription(), matAttributeName);

            defineData =
                    defineData.replace(symbolicAttributeProperty.getUsing(), symbolicAttributeProperty.getSymbolicName() + ".Unknown");
        }
    }


    @Override
    public String createCql() {
        return defineData.toString();
    }

}

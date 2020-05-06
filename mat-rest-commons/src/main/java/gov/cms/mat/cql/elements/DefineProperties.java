package gov.cms.mat.cql.elements;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Builder
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class DefineProperties extends BaseProperties {
    public static final String DEFINE_SDE = "define \"SDE ";

    String defineData;
    String line;
    List<SymbolicProperty> symbolicProperties;

    @Override
    public void setToFhir() {
        if (line.contains(DEFINE_SDE)) {
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
        defineData = StringUtils.replace(defineData,
                '"' + symbolicProperty.getMatDataTypeDescription() + '"',
                '"' + symbolicProperty.getHelper().convertType(symbolicProperty.getMatDataTypeDescription()) + '"');

        if (CollectionUtils.isNotEmpty(symbolicProperty.getAttributePropertySet())) {
            symbolicProperty.getAttributePropertySet()
                    .forEach(a -> processAttribute(a, symbolicProperty));
        }

    }

    private void processAttribute(SymbolicAttributeProperty symbolicAttributeProperty,
                                  SymbolicProperty symbolicProperty) {
        String qdmType = symbolicAttributeProperty.getSymbolicName();
        String qdmAttrib = symbolicAttributeProperty.getMatAttributeName();

        defineData = defineData.replace(symbolicAttributeProperty.getUsing(),
                symbolicProperty.getHelper().convertTypeAndAttribute(qdmType, qdmAttrib));
    }


    @Override
    public String createCql() {
        return defineData;
    }

}

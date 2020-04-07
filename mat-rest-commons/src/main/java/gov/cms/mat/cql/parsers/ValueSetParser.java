package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.ValueSetProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface ValueSetParser {
    String[] getLines();

    default List<ValueSetProperties> getValueSets() {
        return Arrays.stream(getLines())
                .filter(l -> l.startsWith("valueset"))
                .map(this::buildValueSetProperties)
                .collect(Collectors.toList());
    }

    default ValueSetProperties buildValueSetProperties(String line) {
        return ValueSetProperties.builder()
                .line(line)
                .name(findName(line))
                .urnOid(findOid(line))
                .build();
    }


    default String findName(String line) {
        return StringUtils.substringBetween(line, "\"", "\"");
    }

    default String findOid(String line) {
        return StringUtils.substringBetween(line, "'", "'");
    }


}

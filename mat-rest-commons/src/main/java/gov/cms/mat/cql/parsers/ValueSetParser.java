package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.ValueSetProperties;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public interface ValueSetParser extends OidParser, CommentParser {
    String[] getLines();

    default List<ValueSetProperties> getValueSets() {
        AtomicBoolean isInComment = new AtomicBoolean(false);

        return Arrays.stream(getLines())
                .filter(l -> !lineComment(l, isInComment))
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
}

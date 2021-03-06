package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.CodeSystemProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public interface CodeSystemParser extends OidParser, CommentParser {
    String[] getLines();

    default List<CodeSystemProperties> getCodeSystems() {
        AtomicBoolean isInComment = new AtomicBoolean(false);

        return Arrays.stream(getLines())
                .filter(l -> !lineComment(l, isInComment))
                .filter(l -> l.startsWith("codesystem"))
                .map(this::buildCodeSystemProperties)
                .collect(Collectors.toList());
    }

    default CodeSystemProperties buildCodeSystemProperties(String line) {
        return CodeSystemProperties.builder()
                .line(line)
                .name(findName(line))
                .urnOid(findOid(line))
                .version(findVersion(line))
                .build();
    }

    private String findVersion(String line) {
        return StringUtils.substringBetween(line, "version '", "'");
    }
}

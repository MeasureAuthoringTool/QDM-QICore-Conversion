package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.IncludeProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface IncludeParser {
    String[] getLines();

    default List<IncludeProperties> getIncludes() {
        return Arrays.stream(getLines())
                .filter(l -> l.startsWith("include"))
                .map(this::buildIncludeProperties)
                .collect(Collectors.toList());
    }

    default IncludeProperties buildIncludeProperties(String line) {
        return IncludeProperties.builder()
                .line(line)
                .name(findIncludeName(line))
                .version(findIncludeVersion(line))
                .using(getIncludeUsing(line))
                .called(findCalled(line))
                .build();
    }

    default String findCalled(String line) {
        String called = StringUtils.substringAfter(line, "called ");

        if (StringUtils.isEmpty(called)) {
            return StringUtils.EMPTY;
        } else {
            return called.trim();
        }
    }

    default String findIncludeName(String line) {
        return StringUtils.substringBetween(line, "include ", " version ");
    }

    default String findIncludeVersion(String line) {
        return StringUtils.substringBetween(line, " version '", "' ");
    }

    default String getIncludeUsing(String line) {
        String using = StringUtils.substringAfter(line, "using ");

        if (StringUtils.isEmpty(using)) {
            return StringUtils.EMPTY;
        } else {
            return "using " + using;
        }
    }
}

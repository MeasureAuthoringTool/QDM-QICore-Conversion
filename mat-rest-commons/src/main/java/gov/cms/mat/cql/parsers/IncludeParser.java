package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.IncludeProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public interface IncludeParser extends CommentParser {
    String[] getLines();

    default List<IncludeProperties> getIncludes() {
        AtomicBoolean isInComment = new AtomicBoolean(false);

        return Arrays.stream(getLines())
                .filter(l -> !lineComment(l, isInComment))
                .filter(l -> l.startsWith("include"))
                .map(this::buildIncludeProperties)
                .collect(Collectors.toList());
    }



    private IncludeProperties buildIncludeProperties(String line) {
        return IncludeProperties.builder()
                .line(line)
                .name(findIncludeName(line))
                .version(findIncludeVersion(line))
                .using(getIncludeUsing(line))
                .called(findCalled(line))
                .build();
    }

    private String findCalled(String line) {
        String called = StringUtils.substringAfter(line, "called ");

        if (StringUtils.isEmpty(called)) {
            return StringUtils.EMPTY;
        } else {
            return called.trim();
        }
    }

    private String findIncludeName(String line) {
        return StringUtils.substringBetween(line, "include ", " version ");
    }

    private String findIncludeVersion(String line) {
        return StringUtils.substringBetween(line, " version '", "' ");
    }

    private String getIncludeUsing(String line) {
        String using = StringUtils.substringAfter(line, "using ");

        if (StringUtils.isEmpty(using)) {
            return StringUtils.EMPTY;
        } else {
            return "using " + using;
        }
    }
}

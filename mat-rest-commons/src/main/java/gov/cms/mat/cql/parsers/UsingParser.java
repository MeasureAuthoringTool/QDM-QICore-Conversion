package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.UsingProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public interface UsingParser extends CommentParser {
    String[] getLines();

    default UsingProperties getUsing() {
        AtomicBoolean isInComment = new AtomicBoolean(false);

        return Arrays.stream(getLines())
                .filter(l -> !lineComment(l, isInComment))
                .filter(l -> l.startsWith("using"))
                .map(this::buildUsingProperties)
                .findFirst()
                .orElse(null);
    }

    private UsingProperties buildUsingProperties(String line) {
        return UsingProperties.builder()
                .libraryType(getUsingLibraryType(line))
                .version(getUsingLibraryVersion(line))
                .line(line)
                .comment(getCommentAtEnd(line))
                .build();
    }

    private String getUsingLibraryType(String line) {
        return StringUtils.substringBetween(line, "using ", " version ");
    }

    private String getUsingLibraryVersion(String line) {
        return StringUtils.substringBetween(line, " version '", "'");
    }
}

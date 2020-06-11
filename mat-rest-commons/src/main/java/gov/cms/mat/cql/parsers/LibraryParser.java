package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.LibraryProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public interface LibraryParser extends CommentParser {
    String[] getLines();

    default LibraryProperties getLibrary() {
        AtomicBoolean isInComment = new AtomicBoolean(false);

        return Arrays.stream(getLines())
                .filter(l -> !lineComment(l, isInComment))
                .filter(l -> l.startsWith("library"))
                .map(this::buildLibraryProperties)
                .findFirst()
                .orElseThrow(() -> new ParseException("Cannot find library"));
    }

    private LibraryProperties buildLibraryProperties(String line) {
        return LibraryProperties.builder()
                .name(getLibraryName(line))
                .version(getLibraryVersion(line))
                .line(line)
                .comment(getCommentAtEnd(line))
                .build();
    }

    private String getLibraryName(String line) {
        return StringUtils.substringBetween(line, "library ", " version ");
    }

    private String getLibraryVersion(String line) {
        return StringUtils.substringBetween(line, " version '", "'");
    }
}

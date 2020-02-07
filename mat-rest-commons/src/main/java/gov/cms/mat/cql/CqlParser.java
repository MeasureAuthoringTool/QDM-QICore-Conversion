package gov.cms.mat.cql;


import gov.cms.mat.cql.parsers.IncludeParser;
import gov.cms.mat.cql.parsers.LibraryParser;
import gov.cms.mat.cql.parsers.UsingParser;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CqlParser implements IncludeParser, UsingParser, LibraryParser {
    final String cql;

    @Getter
    final String[] lines;

    public CqlParser(String cql) {
        this.cql = cql;
        lines = cql.split("\\r?\\n");
    }


    @Builder
    @Getter
    @ToString
    public static class IncludeProperties {
        String name;
        String version;
    }

    @Builder
    @Getter
    @ToString
    public static class LibraryProperties {
        String name;
        String version;
    }

    @Builder
    @Getter
    @ToString
    public static class UsingProperties {
        String libraryType;
        String version;
    }


}

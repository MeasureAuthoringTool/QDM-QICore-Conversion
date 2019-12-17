package gov.cms.mat.cql_elm_translation.cql_translator;

import gov.cms.mat.cql_elm_translation.exceptions.QdmLibraryParseError;
import gov.cms.mat.cql_elm_translation.exceptions.QdmVersionNotfound;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MatLibrarySourceParser {
    private static final String QDM_VERSION_TAG = "using QDM version";
    private static final String LIBRARY_TAG = "library ";
    private static final String VERSION_TAG = "version";

    private final String cql;

    public MatLibrarySourceParser(String cql) {
        this.cql = cql;
    }

    public ParseResults parse() {
        String qdmVersion = findQdmVersion();

        List<LibraryItem> libraries = findlibraries();

        return new ParseResults(qdmVersion, libraries);
    }

    private List<LibraryItem> findlibraries() {
        return getLines().stream()
                .filter(s -> s.startsWith(LIBRARY_TAG))
                .map(this::findLibrary)
                .collect(Collectors.toList());
    }

    private LibraryItem findLibrary(String s) {
        String libraryData = s.replace(LIBRARY_TAG, "");
        libraryData = libraryData.replace(VERSION_TAG, ",").trim();

        String[] data = libraryData.split(",");

        if (data.length != 2) {
            throw new QdmLibraryParseError(s);
        }

        String name = data[0].trim();
        String libraryVersion = data[1].replace("'", "").trim();

        return LibraryItem.builder()
                .name(name)
                .libraryVersion(libraryVersion)
                .build();
    }

    private String findQdmVersion() {
        return getLines().stream()
                .filter(s -> s.startsWith(QDM_VERSION_TAG))
                .map(this::findVersion)
                .findFirst()
                .orElseThrow(QdmVersionNotfound::new);

    }

    private String findVersion(String s) {
        String version = s.replace(QDM_VERSION_TAG, "").trim();
        return version.replace("'", "");
    }

    public List<String> getLines() {
        return new BufferedReader(new StringReader(cql))
                .lines()
                .collect(Collectors.toList());
    }

    @Builder
    @Data
    public static class LibraryItem {
        String name;
        String libraryVersion;
    }

    @Data
    @AllArgsConstructor
    public static class ParseResults {
        @NonNull String qdmVersion;
        List<LibraryItem> libraries = new ArrayList<>();
    }
}

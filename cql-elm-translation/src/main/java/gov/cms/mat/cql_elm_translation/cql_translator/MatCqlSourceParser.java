package gov.cms.mat.cql_elm_translation.cql_translator;

import gov.cms.mat.cql_elm_translation.exceptions.QdmVersionNotfound;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

public class MatCqlSourceParser {
    private static final String QDM_VERSION_TAG = "using QDM version";

    private final String cql;

    public MatCqlSourceParser(String cql) {
        this.cql = cql;
    }

    public String parseQdmVersion() {
        return getLines().stream()
                .filter(line -> line.startsWith(QDM_VERSION_TAG))
                .map(this::findVersion)
                .findFirst()
                .orElseThrow(QdmVersionNotfound::new);
    }

    private String findVersion(String line) {
        //E.G. -> using QDM version '5.4'
        String version = line.replace(QDM_VERSION_TAG, "").trim();
        return version.replace("'", "");
    }

    public List<String> getLines() {
        return new BufferedReader(new StringReader(cql))
                .lines()
                .collect(Collectors.toList());
    }
}

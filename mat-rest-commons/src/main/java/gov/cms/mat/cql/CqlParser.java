package gov.cms.mat.cql;


import gov.cms.mat.cql.elements.BaseProperties;
import gov.cms.mat.cql.parsers.DefineParser;
import gov.cms.mat.cql.parsers.IncludeParser;
import gov.cms.mat.cql.parsers.LibraryParser;
import gov.cms.mat.cql.parsers.UsingParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CqlParser implements IncludeParser, UsingParser, LibraryParser, DefineParser {
    @Getter
    final String[] lines;
    @Getter
    String cql;

    public CqlParser(String cql) {
        this.cql = cql;
        lines = cql.split("\\r?\\n");
    }

    public void setToFhir(BaseProperties baseProperty) {
        cql = cql.replace(baseProperty.getLine(), baseProperty.createCql());
    }
}

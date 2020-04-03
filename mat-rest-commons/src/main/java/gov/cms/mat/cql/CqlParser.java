package gov.cms.mat.cql;


import gov.cms.mat.cql.elements.BaseProperties;
import gov.cms.mat.cql.parsers.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class CqlParser implements
        IncludeParser, UsingParser, LibraryParser, DefineParser, UnionParser, ValueSetParser {

    final String[] lines;
    String cql;

    public CqlParser(String cql) {
        this.cql = cql;
        lines = cql.split("\\r?\\n");
    }

    public void setToFhir(BaseProperties baseProperty) {
        cql = cql.replace(baseProperty.getLine(), baseProperty.createCql());
    }
}

package gov.cms.mat.cql;


import gov.cms.mat.cql.elements.BaseProperties;
import gov.cms.mat.cql.parsers.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CqlParser implements IncludeParser, UsingParser, LibraryParser, DefineParser, UnionParser {
    @Getter
    final String[] lines;
    @Getter
    String cql;

    public CqlParser(String cql) {
        this.cql = preProcessCQL(cql);
        lines = cql.split("\\r?\\n");
    }
    
    private String preProcessCQL(String cqlString) {
        String[] cqlLines = cqlString.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cqlLines.length; i++) {
            String s = cqlLines[i];
            if (s.indexOf("using") > -1) {
                sb.append("\nusing FHIR version '4.0.0'\n\n");
                sb.append("include FHIRHelpers version '4.0.0'\n");
                sb.append("include SupplementalDataElements_FHIR4 version '1.0.0'\n");
                sb.append("include MATGlobalCommonFunctions_FHIR4 version '4.0.000'\n\n");
            }
            else {
                sb.append(s + "\n");
            }
            i++;
        }
        String res = sb.toString();
        return res;
    }
    
    

    public void setToFhir(BaseProperties baseProperty) {
        cql = cql.replace(baseProperty.getLine(), baseProperty.createCql());
    }
}

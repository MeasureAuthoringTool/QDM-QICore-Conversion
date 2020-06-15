package gov.cms.mat.fhir.services.cql.parser;

public interface CqlParser {
    void parse(String cql, CqlVisitor v);
}

package gov.cms.mat.cql.elements;

public abstract class BaseProperties {
    static final String LIBRARY_FHIR_EXTENSION = "_FHIR";
    static final String LIBRARY_FHIR_TYPE = "FHIR";
    static final String LIBRARY_FHIR_VERSION = "4.0.0";

    public abstract String createCql();

    public abstract String getLine();

    public abstract void setToFhir();
}
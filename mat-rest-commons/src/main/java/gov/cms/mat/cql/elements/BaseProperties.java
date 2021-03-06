package gov.cms.mat.cql.elements;

public abstract class BaseProperties {
    public static final String LIBRARY_FHIR_EXTENSION = "FHIR4";
    public static final String LIBRARY_FHIR_TYPE = "FHIR";
    public static final String LIBRARY_FHIR_VERSION = "4.0.1";

    public abstract String createCql();

    public abstract String getLine();

    public abstract void setToFhir();
}

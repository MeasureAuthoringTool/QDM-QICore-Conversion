package gov.cms.mat.fhir.rest.dto;

public enum ConversionOutcome {
    VALUESET_VALIDATION_FAILED,
    VALUESET_CONVERSION_FAILED,
    LIBRARY_VALIDATION_FAILED,
    LIBRARY_CONVERSION_FAILED,
    LIBRARY_FHIR_CONVERSION_FAILED,
    CQLLIBRARY_NOT_FOUND,
    CQL_LIBRARY_TRANSLATION_FAILED,
    INTERNAL_SERVER_ERROR,
    INVALID_MEASURE_XML,
    MEASURE_VALIDATION_FAILED,
    MEASURE_CONVERSION_FAILED,
    MEASURE_XML_NOT_FOUND,
    MEASURE_RELEASE_VERSION_INVALID,
    MEASURE_NOT_FOUND,
    MEASURE_EXPORT_NOT_FOUND,
    OUTCOME_MISSING,
    QDM_MAPPING_ERROR,
    SUCCESS_WITH_ERROR,
    SUCCESS
}

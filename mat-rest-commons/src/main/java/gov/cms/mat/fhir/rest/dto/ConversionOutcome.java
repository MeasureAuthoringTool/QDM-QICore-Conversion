package gov.cms.mat.fhir.rest.dto;

public enum ConversionOutcome {
    VALUESET_VALIDATION_FAILED,
    VALUESET_CONVERSION_FAILED,
    LIBRARY_VALIDATION_FAILED,
    LIBRARY_CONVERSION_FAILED,
    CQLLIBRARY_NOT_FOUND,
    CQL_LIBRARY_TRANSLATION_FAILED,
    MEASURE_VALIDATION_FAILED,
    MEASURE_CONVERSION_FAILED,
    MEASURE_RELEASE_VERSION_INVALID,
    MEASURE_NOT_FOUND,
    SUCCESS
}

package gov.cms.mat.fhir.rest.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class CqlConversionResult {
    ConversionType type;
    Boolean result;

    /* informative strings of errors */
    List<String> errors = new ArrayList<>();

    /* QDM */
    String cql;
    String elm;

    /* results added automatically by cql-elm-translation service when translating qdm elm */
    Set<CqlConversionError> cqlConversionErrors = new HashSet<>(); // results added automatically by elm  service

    /* results we add for exceptions cql-elm-translation service misses for some reason when translating qdm elm*/
    Set<MatCqlConversionException> matCqlConversionErrors = new HashSet<>();

    String fhirCql;
    String fhirElm;

    /* results added automatically by cql-elm-translation service when translating fhir elm */
    Set<CqlConversionError> fhirCqlConversionErrors = new HashSet<>();
    /* results we add for exceptions cql-elm-translation service misses for some reason when translating fhir elm */
    Set<MatCqlConversionException> fhirMatCqlConversionErrors = new HashSet<>();
}

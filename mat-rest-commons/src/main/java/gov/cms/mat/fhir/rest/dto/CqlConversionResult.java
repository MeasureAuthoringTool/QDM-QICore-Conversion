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
    List<String> errors = new ArrayList<>();
    String cql;
    String elm;
    List<CqlConversionError> cqlConversionErrors = new ArrayList<>();
    Set<MatCqlConversionException> matCqlConversionErrors = new HashSet<>();
}

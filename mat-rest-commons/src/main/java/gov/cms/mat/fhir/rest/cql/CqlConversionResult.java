package gov.cms.mat.fhir.rest.cql;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class CqlConversionResult {
    ConversionType type;
    Boolean result;
    List<String> errors;
    String cql;
    String elm;
    List<CqlConversionError> cqlConversionErrors;
    Set<MatCqlConversionException> matCqlConversionErrors = new HashSet<>();
}

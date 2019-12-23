package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.cql.CqlConversionResult;
import gov.cms.mat.fhir.rest.cql.LibraryConversionResults;
import gov.cms.mat.fhir.rest.cql.MeasureConversionResults;
import gov.cms.mat.fhir.rest.cql.ValueSetConversionResults;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class ConversionResultDto {
    private String measureId;
    private Instant modified;

    private ValueSetConversionResults valueSetConversionResults;

    private MeasureConversionResults measureConversionResults;

    private LibraryConversionResults libraryConversionResults;

    private CqlConversionResult cqlConversionResult;
}

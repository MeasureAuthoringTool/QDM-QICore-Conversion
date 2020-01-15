package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.*;
import gov.cms.mat.fhir.services.components.mongo.helpers.LibraryResultsHelper;
import gov.cms.mat.fhir.services.components.mongo.helpers.MeasureResultsHelper;
import gov.cms.mat.fhir.services.components.mongo.helpers.ValueSetResultsHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document
@Data
@Slf4j
public class ConversionResult implements LibraryResultsHelper, ValueSetResultsHelper, MeasureResultsHelper {
    MeasureConversionResults measureConversionResults;

    List<ValueSetConversionResults> valueSetConversionResults = new ArrayList<>();
    List<LibraryConversionResults> libraryConversionResults = new ArrayList<>();

    @Id
    private String id;
    @Version
    private Long version;
    @CreatedDate
    private Instant created;
    @LastModifiedDate
    private Instant modified;
    @NotBlank
    @Indexed(unique = true)
    private String measureId;

    private String errorReason;
    private ConversionOutcome outcome;

    private ConversionType conversionType;

    private Instant finished;
}

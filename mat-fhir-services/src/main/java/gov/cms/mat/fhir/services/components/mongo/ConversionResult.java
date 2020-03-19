package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.*;
import gov.cms.mat.fhir.services.components.mongo.helpers.LibraryResultsHelper;
import gov.cms.mat.fhir.services.components.mongo.helpers.MeasureResultsHelper;
import gov.cms.mat.fhir.services.components.mongo.helpers.ValueSetResultsHelper;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document
@CompoundIndex(name = "conversion-result-uniq-idx", unique = true, def = "{'sourceMeasureId' : 1, 'start' : 1}")
@Data
@Slf4j
public class ConversionResult implements LibraryResultsHelper, ValueSetResultsHelper, MeasureResultsHelper {
    MeasureConversionResults measureConversionResults;

    List<ValueSetConversionResults> valueSetConversionResults = new ArrayList<>();
    List<LibraryConversionResults> libraryConversionResults = new ArrayList<>();

    private Map<String, String> libraryMappings = new HashMap<>();

    @Id
    private String id;

    private String fhirMeasureId;

    @Version
    private Long version;
    @CreatedDate
    private Instant created;
    @LastModifiedDate
    private Instant modified;
    @NotBlank
    private String sourceMeasureId;

    private String batchId;

    private XmlSource xmlSource;

    private Boolean showWarnings;

    private String errorReason;
    private ConversionOutcome outcome;

    private ConversionType conversionType;

    private Instant start;
    private Instant finished;

    private Instant valueSetsProcessed;

    private String vsacGrantingTicket;
    private String valueSetProcessingMemo;
}

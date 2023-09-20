package gov.cms.mat.fhir.services.components.reporting;

import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.rest.dto.MeasureConversionResults;
import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.components.reporting.helpers.LibraryResultsHelper;
import gov.cms.mat.fhir.services.components.reporting.helpers.MeasureResultsHelper;
import gov.cms.mat.fhir.services.components.reporting.helpers.ValueSetResultsHelper;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class ConversionResult implements LibraryResultsHelper, ValueSetResultsHelper, MeasureResultsHelper {
    MeasureConversionResults measureConversionResults;

    List<ValueSetConversionResults> valueSetConversionResults = new ArrayList<>();
    List<LibraryConversionResults> libraryConversionResults = new ArrayList<>();

    private Map<String, String> libraryMappings = new HashMap<>();

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
    private String vsacApiKey;
    private String valueSetProcessingMemo;
}

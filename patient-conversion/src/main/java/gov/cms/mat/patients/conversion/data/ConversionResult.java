package gov.cms.mat.patients.conversion.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.cms.mat.patients.conversion.dao.ExpectedValues;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class ConversionResult {
    private final String id;

    @JsonProperty("converted_patient")
    private final ConvertedPatient convertedPatient;

    @JsonProperty("expected_values")
    List<ExpectedValues> expectedValues;

    @JsonProperty("measure_ids")
    List<String> measureIds;

    @JsonProperty("data_elements")
    private List<FhirDataElement> dataElements;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;
}

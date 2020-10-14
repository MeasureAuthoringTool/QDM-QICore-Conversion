package gov.cms.mat.patients.conversion.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import gov.cms.mat.patients.conversion.dao.ExpectedValues;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class ConversionResult {
    private final String id;
    @JsonProperty("fhir_patient")
    private final JsonNode fhirPatient;
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


//
//    private final JsonNode encounters;
//    private final JsonNode serviceRequests;
//    private final JsonNode procedures;
//    private final JsonNode medicationRequests;
}

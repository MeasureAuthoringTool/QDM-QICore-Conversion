package gov.cms.mat.patients.conversion.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ConversionResult {
    private final String  patientId;
    private final JsonNode fhirPatient;
    private final JsonNode encounters;
    private final JsonNode serviceRequests;
    private final JsonNode procedures;
    private final JsonNode medicationRequests;
}

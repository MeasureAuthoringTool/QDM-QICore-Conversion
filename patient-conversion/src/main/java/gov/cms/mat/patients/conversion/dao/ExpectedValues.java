package gov.cms.mat.patients.conversion.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExpectedValues {
    @JsonProperty("measure_id")
    String measureId;
    @JsonProperty("population_index")
    Integer populationIndex;
    @JsonProperty("IPP")
    Integer initialPopulation;
    @JsonProperty("DENOM")
    Integer denominator;
    @JsonProperty("DENEX")
    Integer denominatorExclusions;
    @JsonProperty("NUMER")
    Integer numerator;
    @JsonProperty("DENEXCEP")
    Integer denominatorExceptions;
}

package gov.cms.mat.fhir.services.rest.dto;

import lombok.Builder;
import lombok.Getter;
import mat.model.cql.CQLCode;
import mat.model.cql.CQLIncludeLibrary;
import mat.model.cql.CQLQualityDataSetDTO;

import java.util.List;

@Getter
@Builder
public class UnusedCqlElements {
    private final List<CQLIncludeLibrary> libraries;
    private final List<CQLQualityDataSetDTO> valueSets;
    private final List<CQLCode> codes;
}

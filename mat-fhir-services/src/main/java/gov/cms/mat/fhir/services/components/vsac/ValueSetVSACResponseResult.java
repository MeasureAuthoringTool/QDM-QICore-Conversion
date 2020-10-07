package gov.cms.mat.fhir.services.components.vsac;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ValueSetVSACResponseResult {
    private String xmlPayLoad;
    private List<String> pgmRels;
    private boolean isFailResponse;
    private String failReason;
}

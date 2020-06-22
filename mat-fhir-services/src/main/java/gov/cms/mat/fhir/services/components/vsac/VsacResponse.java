package gov.cms.mat.fhir.services.components.vsac;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VsacResponse {
    String message;
    String status;

    VsacData data;
    VsacError errors;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VsacData {
        int resultCount;
        List<VsacDataResultSet> resultSet = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VsacError {
        int errorCount;
        List<VsacErrorResultSet> resultSet = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VsacErrorResultSet {
        String errDesc;
        String errCode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VsacDataResultSet {
        String csName;
        String csOID;
        String csVersion;
        String code;
        String contentMode;
        String codeName;
        String termType;
        String active;
        Long revision;
    }
}

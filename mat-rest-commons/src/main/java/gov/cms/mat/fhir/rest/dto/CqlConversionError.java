package gov.cms.mat.fhir.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;


@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CqlConversionError extends CqlConversionBase {
    String errorSeverity;

    @EqualsAndHashCode.Exclude
    String targetIncludeLibraryId;
    @EqualsAndHashCode.Exclude
    String libraryId;

    @EqualsAndHashCode.Exclude
    String targetIncludeLibraryVersionId;
    @EqualsAndHashCode.Exclude
    String libraryVersion; //added to

    String message;
    String type;

    // Create getters based on old property name for less changes... Need to Revisit
    public String getTargetIncludeLibraryId() {
        if (StringUtils.isNotEmpty(targetIncludeLibraryId)) {
            return targetIncludeLibraryId;
        } else {
            return libraryId;
        }
    }

    public String getTargetIncludeLibraryVersionId() {
        if (StringUtils.isNotEmpty(targetIncludeLibraryVersionId)) {
            return targetIncludeLibraryVersionId;
        } else {
            return libraryVersion;
        }
    }

}

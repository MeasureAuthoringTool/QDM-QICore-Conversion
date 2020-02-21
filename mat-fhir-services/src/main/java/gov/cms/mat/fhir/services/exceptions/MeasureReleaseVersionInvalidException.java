package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
@Slf4j
public class MeasureReleaseVersionInvalidException extends RuntimeException {
    private static final String MESSAGE =
            "MAT Measure with measureId: %s and releaseVersion: %s not in allowedReleaseVersions %s.";

    public MeasureReleaseVersionInvalidException(String measureId, String releaseVersion, String allowedVersions) {
        super(String.format(MESSAGE, measureId, releaseVersion, allowedVersions));
        log.debug(getMessage());
    }
}

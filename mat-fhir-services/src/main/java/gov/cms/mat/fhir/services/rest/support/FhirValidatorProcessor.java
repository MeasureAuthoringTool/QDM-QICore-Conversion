package gov.cms.mat.fhir.services.rest.support;

import ca.uhn.fhir.validation.*;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationError;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FhirValidatorProcessor {
    private final FhirValidator fhirValidator;

    public FhirValidatorProcessor(FhirValidator fhirValidator) {
        this.fhirValidator = fhirValidator;
    }

    public FhirResourceValidationResult validateResource(IBaseResource resource) {
        FhirResourceValidationResult fhirResourceValidationResult = new FhirResourceValidationResult();

        validateResource(fhirResourceValidationResult, resource);

        return fhirResourceValidationResult;
    }

    public void validateResource(FhirResourceValidationResult fhirResourceValidationResult,
                                 IBaseResource resource) {
        validateResource(fhirResourceValidationResult, resource, new ValidationOptions());
    }

    public void validateResource(FhirResourceValidationResult fhirResourceValidationResult,
                                 IBaseResource resource,
                                 ValidationOptions options) {
        ValidationResult validationResult = fhirValidator.validateWithResult(resource, options);

        // Ignore anything below ERROR.
        validationResult.getMessages()
                .stream()
                .filter(m -> m.getSeverity() == ResultSeverityEnum.ERROR || m.getSeverity() == ResultSeverityEnum.FATAL)
                // Here xhtml is validated with some old spec and it fails. This removes those errors.
                .filter(m -> !StringUtils.equals(m.getLocationString(), "Measure.text.div"))
                .forEach(m -> fhirResourceValidationResult.getValidationErrorList().add(buildValidationError(m)));
    }


    private FhirResourceValidationError buildValidationError(SingleValidationMessage next) {
        return new FhirResourceValidationError(next.getSeverity().name(), next.getLocationString(), next.getMessage());
    }

    public List<FhirValidationResult> buildResults(FhirResourceValidationResult response) {
        return response.getValidationErrorList().stream()
                .map(this::buildFhirValidationResult)
                .collect(Collectors.toList());
    }

    private FhirValidationResult buildFhirValidationResult(FhirResourceValidationError e) {
        return FhirValidationResult.builder()
                .severity(e.getSeverity())
                .locationField(e.getLocationField())
                .errorDescription(e.getErrorDescription())
                .build();
    }

    public void checkStandAloneLibrary(CqlLibrary cqlLibrary, String type) {
        if (!type.equals(cqlLibrary.getLibraryModel())) {
            throw new CqlConversionException("Library is not " + type);
        }

        if (cqlLibrary.getMeasureId() != null) {
            throw new CqlConversionException("Library is not standalone");
        }
    }
}

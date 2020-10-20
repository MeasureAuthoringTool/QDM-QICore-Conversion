package gov.cms.mat.patients.conversion.service;

import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {
    private final FhirValidator fhirValidator;

    public ValidationService(FhirValidator fhirValidator) {
        this.fhirValidator = fhirValidator;
    }

    public ValidationResult validate(IBaseResource resource) {
        ValidationOptions options = new ValidationOptions();

        return fhirValidator.validateWithResult(resource, options);
    }
}

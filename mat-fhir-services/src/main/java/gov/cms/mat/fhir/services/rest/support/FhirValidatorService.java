package gov.cms.mat.fhir.services.rest.support;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;

@Service
public class FhirValidatorService  {
    private final FhirValidatorProcessor fhirValidatorProcessor;

    public FhirValidatorService(FhirValidatorProcessor fhirValidatorProcessor) {
        this.fhirValidatorProcessor = fhirValidatorProcessor;
    }

    public FhirResourceValidationResult validate(IBaseResource resource) {
        FhirResourceValidationResult fhirResourceValidationResult = new FhirResourceValidationResult();
        fhirValidatorProcessor.validateResource(fhirResourceValidationResult, resource);
        return fhirResourceValidationResult;
    }
}

package gov.cms.mat.fhir.services.rest.support;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;

@Service
public class FhirValidatorService  {
    private final FhirValidatorProcessor fhirValidatorProcessor;
    private final HapiFhirServer hapiFhirServer;

    public FhirValidatorService(FhirValidatorProcessor fhirValidatorProcessor, HapiFhirServer hapiFhirServer) {
        this.fhirValidatorProcessor = fhirValidatorProcessor;
        this.hapiFhirServer = hapiFhirServer;
    }

    public FhirResourceValidationResult validate(IBaseResource resource) {
        FhirResourceValidationResult fhirResourceValidationResult = new FhirResourceValidationResult();

        fhirValidatorProcessor.validateResource(fhirResourceValidationResult, resource);

        return fhirResourceValidationResult;
    }
}

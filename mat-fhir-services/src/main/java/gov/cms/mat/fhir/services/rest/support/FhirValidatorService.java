package gov.cms.mat.fhir.services.rest.support;


import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;

@Service
public class FhirValidatorService implements FhirValidatorProcessor {
    private final HapiFhirServer hapiFhirServer;

    public FhirValidatorService(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    public FhirResourceValidationResult validate(IBaseResource resource) {
        FhirResourceValidationResult fhirResourceValidationResult = new FhirResourceValidationResult();

        validateResource(fhirResourceValidationResult, resource, hapiFhirServer.getCtx());

        return fhirResourceValidationResult;
    }

}

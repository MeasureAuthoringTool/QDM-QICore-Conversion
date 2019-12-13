package gov.cms.mat.fhir.services.rest.support;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationError;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;

public interface FhirValidatorProcessor {
    default void validateResource(FhirResourceValidationResult res, IBaseResource resource, FhirContext ctx) {
        ca.uhn.fhir.validation.FhirValidator validator = ctx.newValidator();
        validator.registerValidatorModule(new FhirInstanceValidator());

        ValidationResult result = validator.validateWithResult(resource);

        for (SingleValidationMessage next : result.getMessages()) {
            FhirResourceValidationError error =
                    new FhirResourceValidationError(next.getSeverity().name(), next.getLocationString(), next.getMessage());
            res.getErrorList().add(error);
        }
    }
}

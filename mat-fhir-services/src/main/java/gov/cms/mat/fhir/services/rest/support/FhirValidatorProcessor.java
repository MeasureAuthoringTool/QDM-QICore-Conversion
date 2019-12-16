package gov.cms.mat.fhir.services.rest.support;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationError;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;

import java.util.List;
import java.util.stream.Collectors;

public interface FhirValidatorProcessor {
    default void validateResource(FhirResourceValidationResult res, IBaseResource resource, FhirContext ctx) {
        ca.uhn.fhir.validation.FhirValidator validator = ctx.newValidator();
        validator.registerValidatorModule(new FhirInstanceValidator());

        ValidationResult result = validator.validateWithResult(resource);

        for (SingleValidationMessage next : result.getMessages()) {
            FhirResourceValidationError error =
                    new FhirResourceValidationError(next.getSeverity().name(), next.getLocationString(), next.getMessage());
            res.getValidationErrorList().add(error);
        }
    }

    default List<ConversionResult.FhirValidationResult> buildResults(FhirResourceValidationResult response) {
        return response.getValidationErrorList().stream()
                .map(this::processError)
                .collect(Collectors.toList());
    }


    default ConversionResult.FhirValidationResult processError(FhirResourceValidationError e) {
        return ConversionResult.FhirValidationResult.builder()
                .severity(e.getSeverity())
                .locationField(e.getLocationField())
                .errorDescription(e.getErrorDescription())
                .build();
    }

}

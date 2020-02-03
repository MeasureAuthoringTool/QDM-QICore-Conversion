package gov.cms.mat.fhir.services.rest.support;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationError;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;

import java.util.List;
import java.util.stream.Collectors;

public interface FhirValidatorProcessor {
    default void validateResource(FhirResourceValidationResult res, IBaseResource resource, FhirContext ctx) {
        ca.uhn.fhir.validation.FhirValidator validator = ctx.newValidator();

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);
        instanceValidator.setNoTerminologyChecks(true);


        ValidationOptions options = new ValidationOptions();
        //options.addProfile( "http://build.fhir.org/ig/HL7/cqf-measures/branches/R4_Lift/StructureDefinition-library-cqfm.json");

        ValidationResult result = validator.validateWithResult(resource, options);

        for (SingleValidationMessage next : result.getMessages()) {
            FhirResourceValidationError error =
                    new FhirResourceValidationError(next.getSeverity().name(), next.getLocationString(), next.getMessage());
            res.getValidationErrorList().add(error);
        }
    }

    default List<FhirValidationResult> buildResults(FhirResourceValidationResult response) {
        return response.getValidationErrorList().stream()
                .map(this::processError)
                .collect(Collectors.toList());
    }


    default FhirValidationResult processError(FhirResourceValidationError e) {
        return FhirValidationResult.builder()
                .severity(e.getSeverity())
                .locationField(e.getLocationField())
                .errorDescription(e.getErrorDescription())
                .build();
    }
}

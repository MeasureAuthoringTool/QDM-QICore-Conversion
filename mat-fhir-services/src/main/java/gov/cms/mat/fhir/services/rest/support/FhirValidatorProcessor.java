package gov.cms.mat.fhir.services.rest.support;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationError;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;

import java.util.List;
import java.util.stream.Collectors;

public interface FhirValidatorProcessor {
    default FhirResourceValidationResult validateResource(
            IBaseResource resource,
            FhirContext ctx) {
        FhirResourceValidationResult fhirResourceValidationResult = new FhirResourceValidationResult();

        validateResource(fhirResourceValidationResult, resource, ctx);

        return fhirResourceValidationResult;
    }

    default void validateResource(FhirResourceValidationResult fhirResourceValidationResult,
                                  IBaseResource resource,
                                  FhirContext ctx) {
        ca.uhn.fhir.validation.FhirValidator validator = ctx.newValidator();

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);
        instanceValidator.setNoTerminologyChecks(true);

        ValidationOptions options = new ValidationOptions();


        //if (resource instanceof Measure)
        //   options.addProfile("http://build.fhir.org/ig/HL7/cqf-measures/branches/R4_Lift/StructureDefinition-measure-cqfm.json");

        // if (resource instanceof Library)
        //     options.addProfile("http://build.fhir.org/ig/HL7/cqf-measures/branches/R4_Lift/StructureDefinition-library-cqfm.json");


        ValidationResult validationResult = validator.validateWithResult(resource, options);

        validationResult.getMessages()
                .forEach(n -> fhirResourceValidationResult.getValidationErrorList().add(buildValidationError(n)));
    }

    default FhirResourceValidationError buildValidationError(SingleValidationMessage next) {
        return new FhirResourceValidationError(next.getSeverity().name(), next.getLocationString(), next.getMessage());
    }


    default List<FhirValidationResult> buildResults(FhirResourceValidationResult response) {
        return response.getValidationErrorList().stream()
                .map(this::buildFhirValidationResult)
                .collect(Collectors.toList());
    }


    default FhirValidationResult buildFhirValidationResult(FhirResourceValidationError e) {
        return FhirValidationResult.builder()
                .severity(e.getSeverity())
                .locationField(e.getLocationField())
                .errorDescription(e.getErrorDescription())
                .build();
    }

    default void checkStandAloneLibrary(CqlLibrary cqlLibrary, String type) {
        if (!type.equals(cqlLibrary.getLibraryModel())) {
            throw new CqlConversionException("Library is not " + type);
        }

        if (cqlLibrary.getMeasureId() != null) {
            throw new CqlConversionException("Library is not standalone");
        }
    }
}

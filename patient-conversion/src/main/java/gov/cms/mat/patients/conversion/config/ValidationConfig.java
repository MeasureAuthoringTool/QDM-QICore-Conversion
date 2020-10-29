package gov.cms.mat.patients.conversion.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static gov.cms.mat.patients.conversion.conversion.PatientConverter.DETAILED_RACE_URL;
import static gov.cms.mat.patients.conversion.conversion.PatientConverter.US_CORE_RACE_URL;

@Configuration
public class ValidationConfig {

    private final FhirContext fhirContext;

    public ValidationConfig(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }


    @Bean
    FhirValidator fhirValidator() {
        FhirValidator validator = fhirContext.newValidator();

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        instanceValidator.setValidationSupport((IValidationSupport) fhirContext.getValidationSupport());
        instanceValidator.setNoTerminologyChecks(true);

        instanceValidator.setCustomExtensionDomains(US_CORE_RACE_URL, DETAILED_RACE_URL);

        validator.registerValidatorModule(instanceValidator);

        return validator;
    }
}

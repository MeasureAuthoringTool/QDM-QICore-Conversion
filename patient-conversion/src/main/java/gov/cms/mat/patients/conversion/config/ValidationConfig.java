package gov.cms.mat.patients.conversion.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        validator.registerValidatorModule(instanceValidator);

        return validator;
    }
}

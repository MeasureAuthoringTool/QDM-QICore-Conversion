package gov.cms.mat.fhir.services.rest.support;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.config.HapiFhirConfig;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Meta;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class FhirValidatorProcessorTest implements ResourceFileUtil {

    @Test
    void validateResource() throws IOException {

        HapiFhirConfig hapiFhirConfig = new HapiFhirConfig();
        FhirContext ctx = hapiFhirConfig.buildFhirContext();
        String json = getStringFromResource("/libTranslator/library-TJCOverall.json");
        Library library = ctx.newJsonParser().parseResource(Library.class, json);

        log.info(ctx.newJsonParser().setPrettyPrint(true)
                .encodeResourceToString(library));

        FhirValidatorProcessor validatorProcessor = new FhirValidatorProcessor(new FhirValidator(ctx) );

        FhirResourceValidationResult result = validatorProcessor.validateResource(library);

        assertNotNull(result);

        assertTrue(result.getValidationErrorList().isEmpty());
    }
}


package gov.cms.mat.fhir.services.rest.support;

import ca.uhn.fhir.context.FhirContext;
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

        // todo-carson ??? this makes it fail
//        Meta meta = new Meta();
//        meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/library-cqfm");
//        meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/executable-library-cqfm");
//        meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-library-cqfm");
//
//        library.setMeta(meta);

        log.info(ctx.newJsonParser().setPrettyPrint(true)
                .encodeResourceToString(library));

        FhirValidatorProcessor validatorProcessor = new FhirValidatorProcessorUnitTest();

        FhirResourceValidationResult result = validatorProcessor.validateResource(library, ctx);

        assertNotNull(result);

        assertTrue(result.getValidationErrorList().isEmpty());
    }

    class FhirValidatorProcessorUnitTest implements FhirValidatorProcessor {

    }
}


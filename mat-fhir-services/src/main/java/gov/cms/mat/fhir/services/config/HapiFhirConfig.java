package gov.cms.mat.fhir.services.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.validation.CachingValidationSupport;
import org.hl7.fhir.r4.hapi.validation.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class HapiFhirConfig {
    @Bean
    public ValidationSupportChain buildValidationSupportChain(FhirContext ctx, FhirProfiles fhirProfiles) {
        // Create a chain that will hold our modules
        ValidationSupportChain chain = new ValidationSupportChain();

        // DefaultProfileValidationSupport supplies base FHIR definitions. This is generally required
        // even if you are using custom profiles, since those profiles will derive from the base
        // definitions.
        DefaultProfileValidationSupport defaultSupport = new DefaultProfileValidationSupport();
        chain.addValidationSupport(defaultSupport);

        // Create a PrePopulatedValidationSupport which can be used to load custom definitions.
        // In this example we're loading two things, but in a real scenario we might
        // load many StructureDefinitions, ValueSets, CodeSystems, etc.
        PrePopulatedValidationSupport prePopulatedSupport = new PrePopulatedValidationSupport();
        chain.addValidationSupport(prePopulatedSupport);

        // Wrap the chain in a cache to improve performance
        CachingValidationSupport cache = new CachingValidationSupport(chain);

        IParser jsonParser = ctx.newJsonParser();
        fhirProfiles.getProfiles().forEach(r -> {
            try {
                log.info("Loading StructureDefinition: {}", r);
                String json = IOUtils.toString(getResourceAsStream(r));
                StructureDefinition s = jsonParser.parseResource(StructureDefinition.class, json);
                prePopulatedSupport.addStructureDefinition(s);
            } catch (IOException ioe) {
                throw new IOError(ioe);
            }
        });
        ctx.setValidationSupport(cache);
        return chain;
    }


    @Bean
    public FhirContext buildFhirContext() {
        return FhirContext.forR4();
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getClass().getClassLoader().getResourceAsStream(resource);
        return in == null ? getClass().getResourceAsStream(resource) : in;
    }
}

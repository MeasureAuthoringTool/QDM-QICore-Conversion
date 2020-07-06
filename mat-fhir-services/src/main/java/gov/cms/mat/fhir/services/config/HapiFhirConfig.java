package gov.cms.mat.fhir.services.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.validation.CachingValidationSupport;
import org.hl7.fhir.r4.hapi.validation.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class HapiFhirConfig {

    public void setValidationSupportChain(FhirContext ctx) {
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
        try {
            getResourceFiles("/fhir/structure-definition").forEach(r -> {
                try {
                    String json = IOUtils.toString(getResourceAsStream(r));
                    StructureDefinition s = jsonParser.parseResource(StructureDefinition.class, json);
                    prePopulatedSupport.addStructureDefinition(s);
                } catch (IOException ioe) {
                    throw new IOError(ioe);
                }
            });
        } catch (IOException ioe) {
            throw new IOError(ioe);
        }
        ctx.setValidationSupport(cache);
    }

    @Bean
    public FhirContext buildFhirContext() {
        FhirContext result = FhirContext.forR4();
        setValidationSupportChain(result);
        return result;
    }


    private List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();
        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(path + "/" + resource);
            }
        }
        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getClass().getClassLoader().getResourceAsStream(resource);
        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}

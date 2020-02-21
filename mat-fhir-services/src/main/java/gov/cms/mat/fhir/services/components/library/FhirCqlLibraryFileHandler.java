package gov.cms.mat.fhir.services.components.library;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.LibraryProperties;
import gov.cms.mat.fhir.services.config.LibraryConversionFileConfig;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.translate.FhirLibraryTranslator;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class FhirCqlLibraryFileHandler implements FileHandler, FhirCreator {
    private final HapiFhirServer hapiFhirServer;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private final LibraryConversionFileConfig libraryConversionFileConfig;


    public FhirCqlLibraryFileHandler(HapiFhirServer hapiFhirServer,
                                     CQLLibraryTranslationService cqlLibraryTranslationService,
                                     LibraryConversionFileConfig libraryConversionFileConfig) {
        this.hapiFhirServer = hapiFhirServer;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.libraryConversionFileConfig = libraryConversionFileConfig;
    }

    public void loaLibs() {
        libraryConversionFileConfig.getOrder().stream()
                .map(this::getData)
                .forEach(this::processHapiFhir);
    }

    private String getData(String name) {
        try (InputStream i = getClass().getResourceAsStream("/fhir/" + name);) {
            return new String(i.readAllBytes(),"utf8");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void processHapiFhir(String cql) {

        CqlParser cqlParser = new CqlParser(cql);
        LibraryProperties libraryProperties = cqlParser.getLibrary();

        try {
            String uuid = createLibraryUuid(libraryProperties);

            Optional<Library> library = hapiFhirServer.fetchHapiLibrary(uuid);

            AtomicBoolean atomicBoolean = new AtomicBoolean(true);

            if (library.isPresent()) {
                log.info("Already Exists Standard Fhir cql lib url: {}, : Properties: {}",
                        library.get().getUrl(), libraryProperties);
            } else {
                String elm = cqlLibraryTranslationService.convertToJsonFromFhirCql(atomicBoolean, cql);

                FhirLibraryTranslator fhirLibraryTranslator = new FhirLibraryTranslator(cql.getBytes(),
                        elm.getBytes(),
                        hapiFhirServer.getBaseURL());

                Library hapiFhirLibrary = fhirLibraryTranslator.translateToFhir(null);
                String url = hapiFhirServer.persist(hapiFhirLibrary);
                log.info("Created Standard Fhir cql lib url: {}, : Properties: {}", url, libraryProperties);
            }
        } catch (Exception e) {
            log.error("Error processing Standard Fhir cql lib url: {}", libraryProperties, e);
        }
    }

}

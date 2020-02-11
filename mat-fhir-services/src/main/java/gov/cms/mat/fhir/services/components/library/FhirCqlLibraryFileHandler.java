package gov.cms.mat.fhir.services.components.library;


import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.fhir.services.config.LibraryConversionFileConfig;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.translate.FhirLibraryTranslator;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

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
        Path path = checkAndCreatePath(libraryConversionFileConfig.getFhirDirectory());
        log.info("Cql fhir directory is: {}, file order: {}",
                libraryConversionFileConfig.getFhirDirectory(),
                libraryConversionFileConfig.getOrder());

        processFhirLibraries(path);
    }

    private void processFhirLibraries(Path path) {
        libraryConversionFileConfig.getOrder()
                .stream()
                .map(s -> findCqlInFile(path, s))
                .forEach(this::processHapiFhir);
    }

    private void processHapiFhir(String cql) {

        CqlParser cqlParser = new CqlParser(cql);
        CqlParser.LibraryProperties libraryProperties = cqlParser.getLibrary();

        try {
            String uuid = createLibraryUuid(libraryProperties);

            Optional<Library> library = hapiFhirServer.fetchHapiLibrary(uuid);

            AtomicBoolean atomicBoolean = new AtomicBoolean(true);

            if (library.isPresent()) {
                log.info("Already Exists Standard Fhir cql lib url: {}, : Properties: {}",
                        library.get().getUrl(), libraryProperties);
            } else {
                String elm = cqlLibraryTranslationService.convertToJsonFromCql(atomicBoolean, cql);

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

package gov.cms.mat.fhir.services.components.library;


import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class FhirCqlLibraryFileHandler implements FileHandler {
    private static final String EXTENSION = ".cql";
    private final HapiFhirServer hapiFhirServer;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private Path path;
    @Value("${library.fhir.directory}")
    private String directoryName;

    public FhirCqlLibraryFileHandler(HapiFhirServer hapiFhirServer, CQLLibraryTranslationService cqlLibraryTranslationService) {
        this.hapiFhirServer = hapiFhirServer;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
    }

    @PostConstruct
    public void check() {
//        path = checkAndCreatePath(directoryName);
//        log.info("Cql fhir directory is: {}", directoryName);
//
//        processFhirLibraries();
    }

    private void processFhirLibraries() {

        List<String> files = findAll(path);

        files.stream()
                .map(s -> findCqlInFile(path, s))
                .forEach(this::processHapiFhir);

    }

    private void processHapiFhir(String cql) {
        CqlParser cqlParser = new CqlParser(cql);
        CqlParser.LibraryProperties libraryProperties = cqlParser.getLibrary();

        Bundle bundle = hapiFhirServer.getLibraryBundleByVersionAndName(libraryProperties.getVersion(), libraryProperties.getName());

        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        if (CollectionUtils.isEmpty(bundle.getEntry())) {
            String json = cqlLibraryTranslationService.convertToJsonFromCql(atomicBoolean, cql);
            log.info(json);

        } else {
            log.debug("Fhir Library already in fhir: {}", libraryProperties);
        }


    }


}

package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.BaseProperties;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.config.LibraryConversionFileConfig;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.service.support.LibraryConversionReporter;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.LIBRARY_CONVERSION_FAILED;
import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.EXISTS;
import static gov.cms.mat.fhir.services.translate.LibraryTranslatorBase.CQL_CONTENT_TYPE;
import static gov.cms.mat.fhir.services.translate.LibraryTranslatorBase.ELM_CONTENT_TYPE;

@Component
@Slf4j
public class LibraryOrchestrationConversionService extends LibraryOrchestrationBase
        implements FhirLibraryHelper, LibraryConversionReporter, FhirCreator {

    private static final String FAILURE_MESSAGE_PERSIST = "Library conversion failed";
    private final CqlLibraryDataService cqlLibraryDataService;
    private final HapiFhirLinkProcessor hapiFhirLinkProcessor;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;

    private final LibraryConversionFileConfig libraryConversionFileConfig;

    public LibraryOrchestrationConversionService(CqlLibraryDataService cqlLibraryDataService,
                                                 HapiFhirServer hapiFhirServer,
                                                 HapiFhirLinkProcessor hapiFhirLinkProcessor,
                                                 CQLLibraryTranslationService cqlLibraryTranslationService,
                                                 LibraryConversionFileConfig libraryConversionFileConfig) {
        super(hapiFhirServer);
        this.cqlLibraryDataService = cqlLibraryDataService;

        this.hapiFhirLinkProcessor = hapiFhirLinkProcessor;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.libraryConversionFileConfig = libraryConversionFileConfig;
    }

    boolean convert(OrchestrationProperties properties) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        properties.getCqlLibraries()
                .forEach(matLib -> processPersisting(matLib, properties.findFhirLibrary(matLib.getId()), atomicBoolean));

        if (!atomicBoolean.get()) {
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE_PERSIST, LIBRARY_CONVERSION_FAILED);
        }

        return atomicBoolean.get();
    }

    public List<CqlLibrary> getCqlLibrariesNotInHapi(OrchestrationProperties properties) {
        List<CqlLibrary> libraries = cqlLibraryDataService.getCqlLibrariesByMeasureIdRequired(properties.getMeasureId());

        return filterCqlLibraries(libraries);
    }

    public List<CqlLibrary> filterCqlLibraries(List<CqlLibrary> cqlLibraries) {
        return cqlLibraries.stream()
                .filter(this::filterLibrary)
                .collect(Collectors.toList());
    }

    public boolean filterLibrary(CqlLibrary cqlLibrary) {
        Optional<String> optionalLink = findLink(cqlLibrary);

        if (optionalLink.isPresent()) {
            String url = optionalLink.get();

            log.info("Hapi cqlLibrary exists for id: {}, link: {}", cqlLibrary.getId(), url);
            ConversionReporter.setLibraryValidationLink(url, EXISTS, cqlLibrary.getId());

            Optional<Library> optionalLibrary = hapiFhirLinkProcessor.fetchLibraryByUrl(url);

            if (optionalLibrary.isPresent()) {
                Library library = optionalLibrary.get();

                String fhirJson = findContentFromLibrary(library, ELM_CONTENT_TYPE);

                String cleanedFhirJson = cleanJsonFromMatExceptions(fhirJson);
                ConversionReporter.setFhirJson(cleanedFhirJson, cqlLibrary.getId());

                String fhirCql = findContentFromLibrary(library, CQL_CONTENT_TYPE);
                ConversionReporter.setFhirCql(fhirCql, cqlLibrary.getId());

                CqlParser cqlParser = new CqlParser(fhirCql);
                ConversionReporter.setCqlNameAndVersion(cqlParser.getLibrary().getName(),
                        cqlParser.getLibrary().getVersion(),
                        cqlLibrary.getId());


                return cqlLibraryTranslationService.processJsonForError(CQLLibraryTranslationService.ConversionType.FHIR,
                        fhirJson,
                        cqlLibrary.getId());
            } else {
                return true;
            }
        } else {
            ConversionReporter.setLibraryNotFoundInHapi(cqlLibrary.getId());
            return true;
        }
    }

    private Optional<String> findLink(CqlLibrary cqlLibrary) {
        String fhir4Name = cqlLibrary.getCqlName() + BaseProperties.LIBRARY_FHIR_EXTENSION;

        var optionalName = findLibFile(libraryConversionFileConfig.getOrder(), fhir4Name);

        if (optionalName.isPresent()) {
            String version = findVersion(optionalName.get(), null);
            String name = createLibraryUuid(fhir4Name, version);
            return hapiFhirServer.fetchHapiLinkLibrary(name);
        } else {
            return hapiFhirServer.fetchHapiLinkLibrary(cqlLibrary.getId());
        }
    }
}

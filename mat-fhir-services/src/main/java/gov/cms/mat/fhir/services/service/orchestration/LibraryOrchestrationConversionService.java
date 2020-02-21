package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
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
public class LibraryOrchestrationConversionService extends LibraryOrchestrationBase implements FhirLibraryHelper {
    private static final String FAILURE_MESSAGE_PERSIST = "Library conversion failed";
    private final CqlLibraryDataService cqlLibraryDataService;
    private final HapiFhirLinkProcessor hapiFhirLinkProcessor;


    public LibraryOrchestrationConversionService(CqlLibraryDataService cqlLibraryDataService,
                                                 HapiFhirServer hapiFhirServer,
                                                 HapiFhirLinkProcessor hapiFhirLinkProcessor) {
        super(hapiFhirServer);
        this.cqlLibraryDataService = cqlLibraryDataService;

        this.hapiFhirLinkProcessor = hapiFhirLinkProcessor;
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
        Optional<String> optionalLink = hapiFhirServer.fetchHapiLinkLibrary(cqlLibrary.getId());

        if (optionalLink.isPresent()) {
            String url = optionalLink.get();

            log.info("Hapi cqlLibrary exists for id: {}, link: {}", cqlLibrary.getId(), url);
            ConversionReporter.setLibraryValidationLink(url, EXISTS, cqlLibrary.getId());

            Optional<Library> optionalLibrary = hapiFhirLinkProcessor.fetchLibraryByUrl(url);

            if (optionalLibrary.isPresent()) {
                Library library = optionalLibrary.get();

                String fhirJson = findContentFromLibrary(library, ELM_CONTENT_TYPE);
                ConversionReporter.setFhirJson(fhirJson, cqlLibrary.getId());

                String fhirCql = findContentFromLibrary(library, CQL_CONTENT_TYPE);
                ConversionReporter.setFhirCql(fhirCql, cqlLibrary.getId());
            }

            return false;
        } else {
            ConversionReporter.setLibraryNotFoundInHapi(cqlLibrary.getId());
            return true;
        }
    }
}

package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.LIBRARY_CONVERSION_FAILED;
import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.CREATED;
import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.EXISTS;

@Component
@Slf4j
public class LibraryOrchestrationConversionService {
    private static final String FAILURE_MESSAGE = "Library conversion failed";
    private final CqlLibraryDataService cqlLibraryDataService;
    private final HapiFhirServer hapiFhirServer;

    public LibraryOrchestrationConversionService(CqlLibraryDataService cqlLibraryDataService, HapiFhirServer hapiFhirServer) {
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.hapiFhirServer = hapiFhirServer;
    }

    boolean convert(OrchestrationProperties properties) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        properties.getCqlLibraries()
                .forEach(matLib -> processPersisting(matLib, properties.findFhirLibrary(matLib.getId()), atomicBoolean));

        if (!atomicBoolean.get()) {
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE, LIBRARY_CONVERSION_FAILED);
        }

        return atomicBoolean.get();
    }

    private void processPersisting(CqlLibrary matCqlLibrary, Library fhirLibrary, AtomicBoolean atomicBoolean) {
        try {
            String link = hapiFhirServer.persist(fhirLibrary);
            log.debug("Persisted library to Hapi link : {}", link);
            ConversionReporter.setLibraryValidationLink(link, CREATED, matCqlLibrary.getId());
        } catch (Exception e) {
            log.warn("Error Persisting to Hapi, id is for cqlLib: {}", matCqlLibrary.getId(), e);
            ConversionReporter.setLibraryValidationError("HAPI Exception: " + e.getMessage(), matCqlLibrary.getId());
            atomicBoolean.set(false);
        }
    }

    public List<CqlLibrary> getCqlLibrariesNotInHapi(OrchestrationProperties properties) {
        List<CqlLibrary> libraries = cqlLibraryDataService.getCqlLibrariesByMeasureIdRequired(properties.getMeasureId());

        return filterCqlLibraries(libraries);
    }

    public List<CqlLibrary> filterCqlLibraries(List<CqlLibrary> cqlLibraries) {
        return cqlLibraries.stream()
                .filter(this::filterValueSet)
                .collect(Collectors.toList());
    }

    public boolean filterValueSet(CqlLibrary cqlLibrary) {
        Optional<String> optional = hapiFhirServer.fetchHapiLinkLibrary(cqlLibrary.getId());

        if (optional.isPresent()) {
            log.warn("Hapi cqlLibrary exists for id: {}, link: {}", cqlLibrary.getId(), optional.get());
            ConversionReporter.setLibraryValidationLink(optional.get(), EXISTS, cqlLibrary.getId());
            return false;
        } else {
            ConversionReporter.setLibraryNotFoundInHapi(cqlLibrary.getId());
            return true;
        }
    }
}

package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.LIBRARY_CONVERSION_FAILED;
import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.EXISTS;

@Component
@Slf4j
public class LibraryOrchestrationConversionService extends LibraryOrchestrationBase {
    private static final String FAILURE_MESSAGE_PERSIST = "Library conversion failed";
    private final CqlLibraryDataService cqlLibraryDataService;


    public LibraryOrchestrationConversionService(CqlLibraryDataService cqlLibraryDataService, HapiFhirServer hapiFhirServer) {
        super(hapiFhirServer);
        this.cqlLibraryDataService = cqlLibraryDataService;

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

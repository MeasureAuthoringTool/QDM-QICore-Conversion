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
import java.util.stream.Collectors;

@Component
@Slf4j
public class LibraryOrchestrationConversionService {
    private final CqlLibraryDataService cqlLibraryDataService;
    private final HapiFhirServer hapiFhirServer;

    public LibraryOrchestrationConversionService(CqlLibraryDataService cqlLibraryDataService, HapiFhirServer hapiFhirServer) {
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.hapiFhirServer = hapiFhirServer;
    }

    boolean convert(OrchestrationProperties properties) {
        properties.getCqlLibraries()
                .forEach(matLib -> processPersisting(matLib, properties.findFhirLibrary(matLib.getId())));

        return true; //todo
    }

    private void processPersisting(CqlLibrary matCqlLibrary, Library fhirLibrary) {
        try {
            String link = hapiFhirServer.persist(fhirLibrary);
            log.debug("Persisted library to Hapi link : {}", link);
            ConversionReporter.setLibraryValidationLink(link, "Created", matCqlLibrary.getId());
        } catch (Exception e) {
            log.warn("Error Persisting to Hapi, id is for cqlLib: {}", matCqlLibrary.getId(), e);
            ConversionReporter.setLibraryValidationError("HAPI Exception: " + e.getMessage(), matCqlLibrary.getId());
        }
    }

    public List<CqlLibrary> getCqlLibrariesNotInHapi(OrchestrationProperties properties) {
        List<CqlLibrary> libraries = cqlLibraryDataService.getCqlLibrariesByMeasureIdRequired(properties.getMeasureId());

        return filterValueSets(libraries);
    }

    public List<CqlLibrary> filterValueSets(List<CqlLibrary> valueSets) {
        return valueSets.stream()
                .filter(this::filterValueSet)
                .collect(Collectors.toList());
    }

    public boolean filterValueSet(CqlLibrary cqlLibrary) {
        Optional<String> optional = hapiFhirServer.fetchHapiLinkLibrary(cqlLibrary.getId());

        if (optional.isPresent()) {
            log.warn("Hapi cqlLibrary exists for id: {}, link: {}", cqlLibrary.getId(), optional.get());
            ConversionReporter.setLibraryValidationLink(optional.get(), "Exists", cqlLibrary.getId());
            return false;
        } else {
            ConversionReporter.setLibraryNotFoundInHapi(cqlLibrary.getId());
            return true;
        }
    }
}

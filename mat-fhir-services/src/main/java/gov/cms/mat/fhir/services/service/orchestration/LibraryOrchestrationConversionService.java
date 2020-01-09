package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.LibraryTranslator;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
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

        properties.getCqlLibraries().forEach(cqlLibrary -> processPersisting(cqlLibrary));

        return true; //todo
    }

    private void processPersisting(CqlLibrary cqlLib) {

        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        LibraryConversionResults libraryConversionResults = conversionResult.findLibraryConversionResultsRequired(cqlLib.getId());

        String elm = libraryConversionResults.getCqlConversionResult().getElm();
        String cql = libraryConversionResults.getCqlConversionResult().getCql();

        LibraryTranslator fhirMapper = new LibraryTranslator(cqlLib, cql.getBytes(), elm.getBytes(), hapiFhirServer.getBaseURL());
        Library fhirLibrary = fhirMapper.translateToFhir();

        try {
            String link = hapiFhirServer.persist(fhirLibrary);
            ConversionReporter.setLibraryValidationLink(link, "Created", cqlLib.getId());

        } catch (Exception e) {
            log.warn("Error Persisting to Hapi, id is for cqlLib: {}", cqlLib.getId(), e);
            ConversionReporter.setLibraryValidationError("HAPI Exception: " + e.getMessage(), cqlLib.getId());
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
        Optional<String> optional = fetchHapiLink(cqlLibrary.getId());

        if (optional.isPresent()) {
            log.warn("Hapi cqlLibrary exists for id: {}, link: {}", cqlLibrary.getId(), optional.get());
            ConversionReporter.setLibraryValidationLink(optional.get(), "Exists", cqlLibrary.getId());
            return false;
        } else {
            ConversionReporter.setLibraryNotFoundInHapi(cqlLibrary.getId());
            return true;
        }
    }


    public Optional<String> fetchHapiLink(String id) {
        Bundle bundle = hapiFhirServer.getLibrary(id);

        if (bundle.hasEntry()) {
            return Optional.of(bundle.getLink().get(0).getUrl());
        } else {
            return Optional.empty();
        }
    }
}

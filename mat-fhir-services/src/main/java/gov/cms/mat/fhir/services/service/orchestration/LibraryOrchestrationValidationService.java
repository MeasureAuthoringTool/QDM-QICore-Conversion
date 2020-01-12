package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.summary.FhirLibraryResourceValidationResult;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.LibraryTranslator;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LibraryOrchestrationValidationService implements FhirValidatorProcessor {
    private final HapiFhirServer hapiFhirServer;

    public LibraryOrchestrationValidationService(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    boolean validateLibrary(OrchestrationProperties properties) {

        List<Library> libraryList = properties.getCqlLibraries().stream()
                .map(this::translateCqlLib)
                .collect(Collectors.toList());
        properties.getFhirLibraries().addAll(libraryList);

        properties.getCqlLibraries()
                .forEach(c -> validateLibrary(c, properties.findFhirLibrary(c.getId())));

        return true;
    }

    private Library translateCqlLib(CqlLibrary cqlLibrary) {
        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        LibraryConversionResults results = conversionResult.findLibraryConversionResultsRequired(cqlLibrary.getId());

        LibraryTranslator libraryTranslator = new LibraryTranslator(cqlLibrary,
                results.getCqlConversionResult().getElm().getBytes(),
                results.getCqlConversionResult().getCql().getBytes(),
                hapiFhirServer.getBaseURL());

        Library fhirLibrary = libraryTranslator.translateToFhir();

        results.setFhirLibraryJson(hapiFhirServer.toJson(fhirLibrary));

        ConversionReporter.saveConversionResult(conversionResult);

        return fhirLibrary;
    }


    private FhirLibraryResourceValidationResult validateLibrary(CqlLibrary matCqlLibrary, Library fhirLibrary) {
        FhirLibraryResourceValidationResult response = new FhirLibraryResourceValidationResult(matCqlLibrary.getId());
        response.setMeasureId(matCqlLibrary.getMeasureId());

        validateResource(response, fhirLibrary, hapiFhirServer.getCtx());

        List<FhirValidationResult> list = buildResults(response);
        ConversionReporter.setFhirLibraryValidationResults(list, matCqlLibrary.getId());

        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        conversionResult.findOrCreateLibraryConversionResults(matCqlLibrary.getId());

        response.setLibraryConversionResults(conversionResult.getLibraryConversionResults());
        response.setLibraryConversionType(conversionResult.getConversionType());

        return response;
    }


}

package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.support.ErrorSeverityChecker;
import gov.cms.mat.fhir.services.summary.FhirLibraryResourceValidationResult;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.LibraryTranslator;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.LIBRARY_VALIDATION_FAILED;

@Component
@Slf4j
public class LibraryOrchestrationValidationService implements FhirValidatorProcessor, ErrorSeverityChecker {
    private static final String FAILURE_MESSAGE = "Library validation failed";
    private final HapiFhirServer hapiFhirServer;

    public LibraryOrchestrationValidationService(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    boolean validate(OrchestrationProperties properties) {

        List<Library> libraryList = properties.getCqlLibraries().stream()
                .map(this::translateCqlLib)
                .collect(Collectors.toList());

        properties.getFhirLibraries().addAll(libraryList);

        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        properties.getCqlLibraries()
                .forEach(matLib -> validate(matLib, properties.findFhirLibrary(matLib.getId()), atomicBoolean));

        if (!atomicBoolean.get()) {
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE, LIBRARY_VALIDATION_FAILED);
        }

        return atomicBoolean.get();
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


    private FhirLibraryResourceValidationResult validate(CqlLibrary matCqlLibrary, Library fhirLibrary, AtomicBoolean atomicBoolean) {
        FhirLibraryResourceValidationResult response = new FhirLibraryResourceValidationResult(matCqlLibrary.getId());
        response.setMeasureId(matCqlLibrary.getMeasureId());

        validateResource(response, fhirLibrary, hapiFhirServer.getCtx());

        List<FhirValidationResult> list = buildResults(response);
        ConversionReporter.setFhirLibraryValidationResults(list, matCqlLibrary.getId());


        list.forEach(v -> isValid(v, atomicBoolean));

        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        conversionResult.findOrCreateLibraryConversionResults(matCqlLibrary.getId());

        response.setLibraryConversionResults(conversionResult.getLibraryConversionResults());
        response.setLibraryConversionType(conversionResult.getConversionType());

        return response;
    }


}

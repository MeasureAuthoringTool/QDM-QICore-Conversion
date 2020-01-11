package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.LibraryTranslator;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryOrchestrationValidationService {
    private final HapiFhirServer hapiFhirServer;

    public LibraryOrchestrationValidationService(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    boolean validate(OrchestrationProperties properties) {

        properties.getCqlLibraries().forEach(p -> doShit(p));

        return true;
    }

    private void doShit(CqlLibrary cqlLibrary) {
        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        LibraryConversionResults results = conversionResult.findLibraryConversionResultsRequired(cqlLibrary.getId());

        LibraryTranslator libraryTranslator = new LibraryTranslator(cqlLibrary,
                results.getCqlConversionResult().getElm().getBytes(),
                results.getCqlConversionResult().getCql().getBytes(),
                hapiFhirServer.getBaseURL());

        Library fhirLibrary = libraryTranslator.translateToFhir();
        results.setFhirLibrary(fhirLibrary);

        ConversionReporter.saveConversionResult(conversionResult);


    }

}

package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.services.components.mat.DraftMeasureXmlProcessor;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultProcessorService;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushLibraryService {
    private final FhirValidatorProcessor fhirValidatorProcessor;
    private final LibraryOrchestrationService libraryOrchestrationService;
    private final CqlLibraryDataService cqlLibraryDataService;
    private final ConversionResultProcessorService conversionResultProcessorService;
    private final DraftMeasureXmlProcessor draftMeasureXmlProcessor;

    public PushLibraryService(FhirValidatorProcessor fhirValidatorProcessor, LibraryOrchestrationService libraryOrchestrationService,
                              CqlLibraryDataService cqlLibraryDataService,
                              ConversionResultProcessorService conversionResultProcessorService,
                              DraftMeasureXmlProcessor draftMeasureXmlProcessor) {
        this.fhirValidatorProcessor = fhirValidatorProcessor;
        this.libraryOrchestrationService = libraryOrchestrationService;
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.conversionResultProcessorService = conversionResultProcessorService;
        this.draftMeasureXmlProcessor = draftMeasureXmlProcessor;
    }

    public ConversionResultDto convertQdmToFhir(String id, OrchestrationProperties orchestrationProperties) {
        CqlLibrary cqlLibrary = cqlLibraryDataService.findCqlLibraryRequired(id);

        fhirValidatorProcessor.checkStandAloneLibrary(cqlLibrary, "QDM");

        orchestrationProperties.setMeasureLib(cqlLibrary);

        libraryOrchestrationService.process(orchestrationProperties);

        return conversionResultProcessorService.processLibrary(orchestrationProperties.getThreadSessionKey());
    }

    public String convertStandAloneFromMatToFhir(String id, OrchestrationProperties orchestrationProperties) {
        CqlLibrary cqlLibrary = cqlLibraryDataService.findCqlLibraryRequired(id);

        fhirValidatorProcessor.checkStandAloneLibrary(cqlLibrary, "FHIR");

        orchestrationProperties.setMeasureLib(cqlLibrary);

        return draftMeasureXmlProcessor.pushStandAlone(id, cqlLibrary.getCqlXml());
    }
}

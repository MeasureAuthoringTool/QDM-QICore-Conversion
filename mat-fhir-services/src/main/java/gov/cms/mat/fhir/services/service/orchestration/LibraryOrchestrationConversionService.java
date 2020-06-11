package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.FhirLibraryNotFoundInMapException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.service.support.LibraryConversionReporter;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.LIBRARY_CONVERSION_FAILED;

@Component
@Slf4j
public class LibraryOrchestrationConversionService extends LibraryOrchestrationBase
        implements FhirLibraryHelper, LibraryConversionReporter, FhirCreator {

    private static final String FAILURE_MESSAGE_PERSIST = "Library conversion failed";
    private final CqlLibraryDataService cqlLibraryDataService;

    public LibraryOrchestrationConversionService(CqlLibraryDataService cqlLibraryDataService,
                                                 HapiFhirServer hapiFhirServer) {
        super(hapiFhirServer);
        this.cqlLibraryDataService = cqlLibraryDataService;
    }

    public boolean convert(OrchestrationProperties properties) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        persist(properties, atomicBoolean, properties.getMeasureLib());
        if (!atomicBoolean.get()) {
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE_PERSIST, LIBRARY_CONVERSION_FAILED);
        }
        return atomicBoolean.get();
    }

    private void persist(OrchestrationProperties properties, AtomicBoolean atomicBoolean, CqlLibrary matLib) {
        var optional = ConversionReporter.findFhirLibraryId(matLib.getId());

        if (optional.isPresent()) {
            processPersisting(matLib, properties.findFhirLibrary(optional.get()), atomicBoolean);
        } else {
            throw new FhirLibraryNotFoundInMapException(matLib.getId());
        }
    }

    public CqlLibrary getCqlLibRequired(OrchestrationProperties properties) {
        return cqlLibraryDataService.getMeasureLib(properties.getMeasureId());
    }
}

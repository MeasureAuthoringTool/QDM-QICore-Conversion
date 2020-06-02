package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;

import java.util.concurrent.atomic.AtomicBoolean;

import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.CREATED;

@Slf4j
public class LibraryOrchestrationBase {

    final HapiFhirServer hapiFhirServer;

    public LibraryOrchestrationBase(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    void processPersisting(CqlLibrary matCqlLibrary, Library fhirLibrary, AtomicBoolean atomicBoolean) {
        try {
            String link = hapiFhirServer.persist(fhirLibrary);
            log.debug("Persisted library to Hapi link : {}", link);
            ConversionReporter.setLibraryValidationLink(link, CREATED, matCqlLibrary.getId());
        } catch (Exception e) {
            log.warn("Error Persisting to Hapi, id is for cqlLib: {}", matCqlLibrary.getId(), e);
            ConversionReporter.setLibraryValidationError(fhirLibrary.getUrl(), "HAPI Exception: " + e.getMessage(), matCqlLibrary.getId());
            atomicBoolean.set(false);
        }
    }

}

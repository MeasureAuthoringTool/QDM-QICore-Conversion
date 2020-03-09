package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.HapiFhirCreateException;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.MEASURE_CONVERSION_FAILED;
import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.CREATED;
import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.EXISTS;

@Component
@Slf4j
public class MeasureOrchestrationConversionService {
    private static final String FAILURE_MESSAGE = "Measure conversion failed";
    private final HapiFhirServer hapiFhirServer;
    private final HapiFhirLinkProcessor hapiFhirLinkProcessor;

    public MeasureOrchestrationConversionService(HapiFhirServer hapiFhirServer, HapiFhirLinkProcessor hapiFhirLinkProcessor) {
        this.hapiFhirServer = hapiFhirServer;
        this.hapiFhirLinkProcessor = hapiFhirLinkProcessor;
    }

    void processExistingFhirMeasure(OrchestrationProperties properties) {
        Optional<Measure> optional = hapiFhirServer.fetchHapiMeasure(properties.getMeasureId());

        if (optional.isPresent()) {
            ConversionReporter.setFhirMeasureJson(hapiFhirServer.toJson(optional.get()));
            ConversionReporter.setMeasureValidationLink(optional.get().getUrl(), EXISTS);
            log.debug("FhirMeasure exists for id: {}", properties.getMeasureId());
        } else {
            log.debug("FhirMeasure does NOT exists for id: {}", properties.getMeasureId());
        }
    }

    boolean convert(OrchestrationProperties properties) {
//        if (ConversionReporter.getConversionResult().measureExistsInHapi()) {
//            log.info("No Conversion performed already in hapi measureId: {}", properties.getMeasureId());
//            return true;
//        } else {
            log.info("Converting measure hapi measureId: {}", properties.getMeasureId());
            return persistToFhir(properties);
//        }
    }

    private boolean persistToFhir(OrchestrationProperties properties) {
        try {
            String link = hapiFhirServer.persist(properties.getFhirMeasure());

            ConversionReporter.setMeasureValidationLink(link, CREATED);

            Optional<Measure> optional = hapiFhirLinkProcessor.fetchMeasureByUrl(link);

            if (optional.isPresent()) {
                ConversionReporter.setFhirMeasureJson(hapiFhirServer.toJson(optional.get()));
            } else {
                throw new HapiFhirCreateException("Cannot find Measure json for url: " + link);
            }

            return true;
        } catch (Exception e) {
            log.error(FAILURE_MESSAGE, e);
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE, MEASURE_CONVERSION_FAILED);

            return false;
        }
    }
}

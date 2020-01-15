package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
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

    public MeasureOrchestrationConversionService(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    void processExistingFhirMeasure(OrchestrationProperties properties) {
        Optional<String> optional = hapiFhirServer.fetchHapiLinkMeasure(properties.getMeasureId());

        if (optional.isPresent()) {
            log.debug("FhirMeasure exists for id: {}", properties.getMeasureId());
            ConversionReporter.setMeasureValidationLink(optional.get(), EXISTS);
        } else {
            log.debug("FhirMeasure does NOT exists for id: {}", properties.getMeasureId());
        }
    }

    boolean convert(OrchestrationProperties properties) {
        if (ConversionReporter.getConversionResult().measureExistsInHapi()) {
            log.info("No Conversion performed already in hapi measureId: {}", properties.getMeasureId());
            return true;
        } else {
            log.info("Converting measure hapi measureId: {}", properties.getMeasureId());
            return convertMeasure(properties);
        }
    }

    private boolean convertMeasure(OrchestrationProperties properties) {
        try {
            String link = hapiFhirServer.persist(properties.getFhirMeasure());

            ConversionReporter.setMeasureValidationLink(link, CREATED);

            return true;
        } catch (Exception e) {
            log.error(FAILURE_MESSAGE, e);
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE, MEASURE_CONVERSION_FAILED);

            return false;
        }
    }
}

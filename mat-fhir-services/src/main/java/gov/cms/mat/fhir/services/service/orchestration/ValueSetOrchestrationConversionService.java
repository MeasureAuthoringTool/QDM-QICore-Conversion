package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.HapiResourcePersistedState;
import gov.cms.mat.fhir.services.exceptions.HapiFhirCreateMeasureException;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static gov.cms.mat.fhir.services.components.reporting.HapiResourcePersistedState.CREATED;
import static gov.cms.mat.fhir.services.components.reporting.HapiResourcePersistedState.EXISTS;

@Component
@Slf4j
public class ValueSetOrchestrationConversionService {
    private static final String FAILURE_ERROR_MESSAGE = "ValueSet conversion failed";
    private final HapiFhirServer hapiFhirServer;
    private final HapiFhirLinkProcessor hapiFhirLinkProcessor;

    public ValueSetOrchestrationConversionService(HapiFhirServer hapiFhirServer, HapiFhirLinkProcessor hapiFhirLinkProcessor) {
        this.hapiFhirServer = hapiFhirServer;
        this.hapiFhirLinkProcessor = hapiFhirLinkProcessor;
    }

    boolean convert(OrchestrationProperties properties) {
        properties.getValueSets()
                .forEach(this::processPersisting);

        long errorCount = countValueSetResultErrors();

        if (errorCount > 0) {
            log.info("{} count: {}", FAILURE_ERROR_MESSAGE, errorCount);
        } else {
            log.debug("ValueSet error processing PASSED");
        }

        return errorCount == 0;
    }

    private long countValueSetResultErrors() {
        return ConversionReporter.getConversionResult()
                .getValueSetConversionResults()
                .stream()
                .filter(t -> BooleanUtils.isFalse(t.getSuccess()))
                .count();
    }

    private void processPersisting(ValueSet valueSetIn) {
        String url = hapiFhirServer.buildResourceUrl(valueSetIn);
        Optional<ValueSet> optional = hapiFhirLinkProcessor.fetchValueSetByUrl(url);

        if (optional.isPresent()) {
            log.debug("ValueSet already in hapiFhir: {}", url);
            addToReport(url, optional.get(), EXISTS);
        } else {
            valueSetIn.setUrl(url);
            persistToFhir(valueSetIn);
        }
    }

    private void addToReport(String url, ValueSet valueSet, HapiResourcePersistedState state) {
        String id = valueSet.getIdElement().getIdPart();
        log.debug("Value set id: {} ", id);

        ConversionReporter.setValueSetsValidationLink(id, url, state);
        ConversionReporter.setValueSetJson(id, hapiFhirServer.toJson(valueSet));
    }

    private void persistToFhir(ValueSet valueSetIn) {
        try {
            String link = hapiFhirServer.persist(valueSetIn);

            Optional<ValueSet> optional = hapiFhirLinkProcessor.fetchValueSetByUrl(link);

            if (optional.isPresent()) {
                log.debug("Persisted valueSet to Hapi link : {}", link);
                addToReport(link, optional.get(), CREATED);
            } else {
                throw new HapiFhirCreateMeasureException("Cannot find ValueSet json for url: " + link);
            }

        } catch (Exception e) {
            log.warn("Error Persisting to Hapi, id is for valueSet: {}", valueSetIn.getId(), e);
            ConversionReporter.setValueSetsValidationError(valueSetIn.getId(), "HAPI Exception: " + e.getMessage());
        }
    }
}
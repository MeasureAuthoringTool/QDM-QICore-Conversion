package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ValueSetOrchestrationConversionService {
    private final HapiFhirServer hapiFhirServer;

    public ValueSetOrchestrationConversionService(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    boolean convert(OrchestrationProperties properties) {
        properties.getValueSets().forEach(this::processPersisting);

        long errorCount = countValueSetResultErrors();

        if (errorCount > 0) {
            log.info("ValueSet error processing FAILED, count: {} ", errorCount);
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

    public List<ValueSet> filterValueSets(List<ValueSet> valueSets) {
        return valueSets.stream()
                .filter(this::filterValueSet)
                .collect(Collectors.toList());
    }

    public boolean filterValueSet(ValueSet valueSet) {
        Optional<String> optional = hapiFhirServer.fetchHapiLinkValueSet(valueSet.getId());

        if (optional.isPresent()) {
            log.info("Hapi valueSet exists for oid: {}", valueSet.getId());
            ConversionReporter.setValueSetsValidationLink(valueSet.getId(), optional.get(), "Exists");
            return false;
        } else {
            ConversionReporter.setValueSetInit(valueSet.getId(), "Hapi valueSet does NOT exist");
            return true;
        }
    }


    private void processPersisting(ValueSet valueSet) {
        Optional<String> optional = hapiFhirServer.fetchHapiLinkValueSet(valueSet.getId());

        if (optional.isPresent()) {
            log.info("ValueSet already in hapiFhir: {}", optional.get());
        } else {
            persistToFhir(valueSet);
        }
    }

    private void persistToFhir(ValueSet valueSetIn) {
        try {
            String link = hapiFhirServer.persist(valueSetIn);
            log.debug("Persisted valueSet to Hapi link : {}", link);
            ConversionReporter.setValueSetsValidationLink(valueSetIn.getId(), link, "Created");
        } catch (Exception e) {
            log.warn("Error Persisting to Hapi, id is for valueSet: {}", valueSetIn.getId(), e);
            ConversionReporter.setValueSetsValidationError(valueSetIn.getId(), "HAPI Exception: " + e.getMessage());
        }
    }
}
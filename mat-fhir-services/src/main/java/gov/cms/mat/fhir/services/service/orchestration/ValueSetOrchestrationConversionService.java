package gov.cms.mat.fhir.services.service.orchestration;


import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ValueSetOrchestrationConversionService {
    private final HapiFhirServer hapiFhirServer;
    private final ValueSetMapper valueSetMapper;

    public ValueSetOrchestrationConversionService(HapiFhirServer hapiFhirServer, ValueSetMapper valueSetMapper) {
        this.hapiFhirServer = hapiFhirServer;
        this.valueSetMapper = valueSetMapper;
    }

    boolean convert(OrchestrationProperties properties) {
        properties.getValueSets()
                .stream()
                .forEach(v -> processPersistToFhir(v));


        return true; // todo how to handle errors
        // valueSetMapper.persistFhirValueSet()

    }

    public List<ValueSet> filterValueSets(List<ValueSet> valueSets) {
        return valueSets.stream()
                .filter(this::filterValueSet)
                .collect(Collectors.toList());
    }

    public boolean filterValueSet(ValueSet valueSet) {
        Optional<String> optional = fetchHapiLink(valueSet.getId());

        if (optional.isPresent()) {
            log.warn("Hapi valueSet exists for oid: {}", valueSet.getId());
            ConversionReporter.setValueSetsValidationLink(valueSet.getId(), optional.get(), "Exists");
            return false;
        } else {
            return true;
        }
    }


    public Optional<String> fetchHapiLink(String oid) {
        Bundle bundle = hapiFhirServer.isValueSetInHapi(oid);

        if (bundle.hasEntry()) {
            return Optional.of(bundle.getLink().get(0).getUrl());
        } else {
            return Optional.empty();
        }
    }

    private void processPersistToFhir(ValueSet valueSet) {
        Optional<String> optional = fetchHapiLink(valueSet.getId());

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
package gov.cms.mat.fhir.services.service.orchestration;


import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
                .forEach(v -> persistToFhir(v));


        return true; // todo how to handle errors
        // valueSetMapper.persistFhirValueSet()

    }

    public Optional<String> fetchHapiLink(ValueSet valueSet) {
        Bundle bundle = hapiFhirServer.isValueSetInHapi(valueSet.getId());

        if (bundle.hasEntry()) {
            return Optional.ofNullable(bundle.getLink().get(0).getUrl());
        } else {
            return Optional.empty();
        }
    }


    private void persistToFhir(ValueSet valueSet) {
        Optional<String> optional = fetchHapiLink(valueSet);

        if (optional.isPresent()) {
            log.info("ValueSet already in hapiFhir: {}", optional.get());
        } else {
            persistToFhir2(valueSet);
        }
    }


    private boolean persistToFhir2(ValueSet valueSetIn) {
        try {
            ValueSet persistValueSet = valueSetMapper.persistFhirValueSet(valueSetIn);

            if (persistValueSet.getId() == null) {
                log.warn("HAPI Persisted id is null for valueSet: {}", valueSetIn.getId());
                ConversionReporter.setValueSetsValidationError(valueSetIn.getId(), "HAPI Persisted id is null");
                return false;
            } else {
                log.debug("Persisted id is for valueSet: {}", valueSetIn.getId());
                return true;
            }
        } catch (Exception e) {
            log.debug("Error Persisting to Hapi, id is for valueSet: {}", valueSetIn.getId(), e);

            return false;

        }

    }
}
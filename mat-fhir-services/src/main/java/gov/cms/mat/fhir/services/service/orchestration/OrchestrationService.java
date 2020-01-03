package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrchestrationService {
    private final ConversionResultsService conversionResultsService;
    private final ValueSetOrchestrationService valueSetOrchestrationService;

    public OrchestrationService(ConversionResultsService conversionResultsService, ValueSetOrchestrationService valueSetOrchestrationService) {
        this.conversionResultsService = conversionResultsService;
        this.valueSetOrchestrationService = valueSetOrchestrationService;
    }

    public boolean validate(OrchestrationProperties properties) {
        ConversionReporter.setInThreadLocal(properties.getMatMeasure().getId(), conversionResultsService);
        ConversionReporter.resetOrchestration();

        return valueSetOrchestrationService.validate(properties);
    }
}

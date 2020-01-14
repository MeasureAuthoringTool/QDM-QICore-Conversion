package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MeasureOrchestrationConversionService {

    private final HapiFhirServer hapiFhirServer;

    public MeasureOrchestrationConversionService(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    boolean convert(OrchestrationProperties properties) {
        String link = hapiFhirServer.persist(properties.getFhirMeasure());

        ConversionReporter.setMeasureValidationLink(link, "Created");

        return true;
    }

}

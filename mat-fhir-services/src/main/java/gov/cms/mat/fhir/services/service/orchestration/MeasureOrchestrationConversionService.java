package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.exceptions.HapiFhirCreateMeasureException;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public boolean convert(OrchestrationProperties properties) {
        log.info("Converting measure hapi measureId: {}", properties.getMeasureId());

        try {
            String link = hapiFhirServer.persist(properties.getFhirMeasure());

            ConversionReporter.setMeasureValidationLink(link, CREATED);

            Optional<Measure> optional = hapiFhirLinkProcessor.fetchMeasureByUrl(link);

            if (optional.isPresent()) {
                Measure fhirMeasure = optional.get();
                ConversionReporter.setFhirMeasureJson(hapiFhirServer.toJson(fhirMeasure));
            } else {
                throw new HapiFhirCreateMeasureException("Cannot find Measure json for url: " + link);
            }

            return true;
        } catch (Exception e) {
            log.error(FAILURE_MESSAGE, e);
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE, MEASURE_CONVERSION_FAILED);

            return false;
        }
    }

    private void addLibrariesLinkToMeasures(Measure fhirMeasure) {
        ConversionResult conversionResult = ConversionReporter.getConversionResult();

        List<CanonicalType> theLibrary = conversionResult.getLibraryConversionResults()
                .stream()
                .filter(result -> StringUtils.isNotEmpty(result.getLink()))
                .map(result -> makeLibraryLinkRelative(result.getLink()))
                .map(CanonicalType::new)
                .collect(Collectors.toList());

        log.debug("Lib size = {}", theLibrary.size());

        fhirMeasure.setLibrary(theLibrary);
    }

    private String makeLibraryLinkRelative(String fullLink) {
        //  "http://localhost:6060/hapi-fhir-jpaserver/fhir/Library/9a2a67acf9fc407eb5bd9c06e2d84fa1"

        int start = fullLink.indexOf("/Library/");

        if (start == -1) {
            return fullLink;
        } else {
            return fullLink.substring(start + 1);
        }
    }
}

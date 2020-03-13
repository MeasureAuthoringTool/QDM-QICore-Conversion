package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.mongo.ThreadSessionKey;
import gov.cms.mat.fhir.services.service.orchestration.VSACOrchestrationService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.*;

@Service
@Slf4j
public class ValueSetBackGroundService {
    private static final String OUTCOME_ERROR_MESSAGE = "Value set processing not performed for outcome: %s";

    private static final ConversionOutcome[] CONVERSION_OUTCOMES_FAILURES = {MEASURE_XML_NOT_FOUND,
            MEASURE_RELEASE_VERSION_INVALID,
            MEASURE_NOT_FOUND,
            MEASURE_EXPORT_NOT_FOUND};

    private final ConversionResultsService conversionResultsService;
    private final MeasureDataService measureDataService;
    private final VSACOrchestrationService vsacOrchestrationService;

    public ValueSetBackGroundService(ConversionResultsService conversionResultsService,
                                     MeasureDataService measureDataService,
                                     VSACOrchestrationService vsacOrchestrationService) {
        this.conversionResultsService = conversionResultsService;
        this.measureDataService = measureDataService;
        this.vsacOrchestrationService = vsacOrchestrationService;
    }

    // @Scheduled(fixedRate = 300000)
    void checkConversionResults() {
        log.info("Running Scheduled");

        while (true) {
            var optional = conversionResultsService.findTopValueSetConversion();

            if (optional.isPresent()) {
                process(optional.get());
            } else {
                break;
            }
        }
    }

    private void process(ConversionResult conversionResult) {

        String memo;

        try {
            ThreadSessionKey threadSessionKey = buildThreadSessionKey(conversionResult);

            if (checkOutCome(conversionResult.getOutcome())) {
                memo = String.format(OUTCOME_ERROR_MESSAGE, conversionResult.getOutcome());
                ConversionReporter.setValueSetCompletionMemo(String.format(OUTCOME_ERROR_MESSAGE, conversionResult.getOutcome()));
            } else {
                orchestrateValueSets(conversionResult, threadSessionKey);
                ConversionReporter.setValueSetCompletionMemo("Orchestrated");
            }
        } finally {
            ConversionReporter.removeInThreadLocal();
        }

    }

    private void orchestrateValueSets(ConversionResult conversionResult, ThreadSessionKey threadSessionKey) {
        Measure matMeasure;

        try {
            matMeasure = measureDataService.findOneValid(conversionResult.getMeasureId());
        } catch (Exception e) {
            log.warn("Cannot orchestrate ValueSets", e);
            ConversionReporter.setValueSetCompletionMemo(e.getMessage());
            return;
        }

        OrchestrationProperties orchestrationProperties = buildProperties(conversionResult, threadSessionKey, matMeasure);
        vsacOrchestrationService.processConversion(orchestrationProperties);
    }

    private OrchestrationProperties buildProperties(ConversionResult conversionResult, ThreadSessionKey threadSessionKey, Measure matMeasure) {
        return OrchestrationProperties.builder()
                .matMeasure(matMeasure)
                .conversionType(conversionResult.getConversionType())
                .xmlSource(conversionResult.getXmlSource())
                .threadSessionKey(threadSessionKey)
                .vsacGrantingTicket(conversionResult.getVsacGrantingTicket())
                .build();
    }

    private ThreadSessionKey buildThreadSessionKey(ConversionResult conversionResult) {
        return ConversionReporter.setInThreadLocal(conversionResult.getMeasureId(),
                conversionResult.getBatchId(),
                conversionResultsService,
                conversionResult.getStart(),
                conversionResult.getConversionType(),
                conversionResult.getXmlSource(),
                conversionResult.getShowWarnings(),
                conversionResult.getVsacGrantingTicket());
    }

    private boolean checkOutCome(ConversionOutcome outcome) {
        if (outcome == null) {
            log.warn("ConversionOutcome is null");
            return false;
        } else {
            var optional = Arrays.stream(CONVERSION_OUTCOMES_FAILURES)
                    .filter(conversionOutcome -> conversionOutcome.equals(outcome))
                    .findFirst();

            return optional.isEmpty();
        }
    }

}

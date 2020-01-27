package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.BatchResultsService;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.BatchIdConflictException;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.summary.BatchResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(path = "/batch")
@Tag(name = "Orchestration-Batch-Controller",
        description = "API for converting many MAT Measures (For testing) to FHIR executing all validations and services to perform this task")
@Slf4j
public class OrchestrationBatchController {
    private final ConversionResultsService conversionResultsService;
    private final OrchestrationController orchestrationController;
    private final MeasureDataService measureDataService;
    private final BatchResultsService batchResultsService;


    public OrchestrationBatchController(ConversionResultsService conversionResultsService,
                                        OrchestrationController orchestrationController,
                                        MeasureDataService measureDataService,
                                        BatchResultsService batchResultsService) {
        this.conversionResultsService = conversionResultsService;
        this.orchestrationController = orchestrationController;
        this.measureDataService = measureDataService;
        this.batchResultsService = batchResultsService;
    }

    @PutMapping
    public Map<ConversionOutcome, BatchResult> translateMeasures(
            @RequestParam ConversionType conversionType,
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource,
            @RequestParam String batchId,
            @RequestBody List<String> matIds) {

        if (CollectionUtils.isEmpty(matIds)) {
            matIds = measureDataService.findAllIds();
        }

        checkBatch(batchId);

        matIds.parallelStream()
                .forEach((id -> orchestrate(conversionType, xmlSource, batchId, id)));

        log.info("Finished OrchestrationBatch with batchId: {} ", batchId);

        return createAggregationBatchReport(batchId);
    }

    @GetMapping
    public Set<String> findBatchIds() {
        return conversionResultsService.findBatchIds();
    }

    @GetMapping(path = "/findReport")
    public Map<ConversionOutcome, BatchResult> findBatchResults(@RequestParam String batchId) {
        return createAggregationBatchReport(batchId);
    }

    private Map<ConversionOutcome, BatchResult> createAggregationBatchReport(String batchId) {
        return batchResultsService.generateAggregationReport(batchId);
    }

    private void orchestrate(ConversionType conversionType,
                             XmlSource xmlSource,
                             String batchId,
                             String id) {
        try {
            orchestrationController.translateMeasureById(id, conversionType, xmlSource, batchId);
        } catch (Exception e) {
            log.info("Error for id: {}, reason: {}", id, e.getMessage());
            log.debug("Error for id: {}", id, e);
        }
    }

    private void checkBatch(String batchId) {
        if (conversionResultsService.checkBatchId(batchId)) {
            log.info("Starting OrchestrationBatch with batchId: {} ", batchId);
        } else {
            throw new BatchIdConflictException(batchId);
        }
    }
}

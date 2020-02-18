package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.BatchResultsService;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.config.health.SelfHealthCheckingService;
import gov.cms.mat.fhir.services.exceptions.BatchIdConflictException;
import gov.cms.mat.fhir.services.exceptions.OrchestrationBatchJobAlreadyRunningException;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.summary.BatchResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping(path = "/batch")
@Tag(name = "Orchestration-Batch-Controller",
        description = "API for converting many MAT Measures (For testing) to FHIR executing all validations and services to perform this task")
@Slf4j
public class OrchestrationBatchController {
    /* long running multi threaded job runs only one at time, */
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);
    private static RunningBatchJobInfo runningBatchJobInfo;

    private final ConversionResultsService conversionResultsService;
    private final OrchestrationController orchestrationController;
    private final MeasureDataService measureDataService;
    private final BatchResultsService batchResultsService;
    private final SelfHealthCheckingService selfHealthCheckingService;


    public OrchestrationBatchController(ConversionResultsService conversionResultsService,
                                        OrchestrationController orchestrationController,
                                        MeasureDataService measureDataService,
                                        BatchResultsService batchResultsService, SelfHealthCheckingService selfHealthCheckingService) {
        this.conversionResultsService = conversionResultsService;
        this.orchestrationController = orchestrationController;
        this.measureDataService = measureDataService;
        this.batchResultsService = batchResultsService;
        this.selfHealthCheckingService = selfHealthCheckingService;
    }

    private static void createRunningInfo(String batchId) {
        runningBatchJobInfo = new RunningBatchJobInfo(batchId);
    }

    @PutMapping
    public Map<String, BatchResult> translateMeasures(
            @RequestParam ConversionType conversionType,
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource,
            @RequestParam String batchId,
            @RequestBody List<String> matIds) {

        selfHealthCheckingService.checkHealthWithException();

        if (isRunning.compareAndSet(false, true)) {
            try {
                List<String> matIdsToProcess = processRequestData(batchId, matIds);

                matIdsToProcess.parallelStream()
                        .forEach((id -> orchestrate(conversionType, xmlSource, batchId, id)));

                log.info("Completed orchestrating {} ids with batchId: {} in {} seconds",
                        matIdsToProcess.size(),
                        batchId,
                        runningBatchJobInfo.computeRunningSeconds());

                Map<String, BatchResult> results = createAggregationBatchReport(batchId);

                log.info("Finished OrchestrationBatch with batchId: {} in {} seconds",
                        batchId,
                        runningBatchJobInfo.computeRunningSeconds());

                return results;
            } finally {
                isRunning.set(false);
            }
        } else {
            Long runningSeconds = runningBatchJobInfo.computeRunningSeconds();
            log.info("Another batch job is already running: {}", runningBatchJobInfo);
            throw new OrchestrationBatchJobAlreadyRunningException(runningBatchJobInfo.batchId, runningSeconds);
        }
    }

    public List<String> processRequestData(String batchId, List<String> matIds) {
        createRunningInfo(batchId);
        checkBatch(batchId);

        if (CollectionUtils.isEmpty(matIds)) {
            return measureDataService.findAllValidIds();
        } else {
            return matIds;
        }
    }

    @GetMapping
    public Set<String> findBatchIds() {
        return conversionResultsService.findBatchIds();
    }

    @GetMapping(path = "/findReport")
    public Map<String, BatchResult> findBatchResults(@RequestParam String batchId) {
        return createAggregationBatchReport(batchId);
    }

    private Map<String, BatchResult> createAggregationBatchReport(String batchId) {
        return batchResultsService.generateAggregationReport(batchId);
    }

    private void orchestrate(ConversionType conversionType,
                             XmlSource xmlSource,
                             String batchId,
                             String id) {
        try {
            orchestrationController.translateMeasureById(id, conversionType, xmlSource, batchId, Boolean.TRUE);
        } catch (Exception e) {
            log.warn("Error for id: {}, reason: {}", id, e.getMessage());
            log.info("Error for id: {}", id, e);
        }
    }

    private void checkBatch(String batchId) {
        if (conversionResultsService.checkBatchIdNotUsed(batchId)) {
            log.info("Starting OrchestrationBatch with batchId: {} ", batchId);
        } else {
            throw new BatchIdConflictException(batchId);
        }
    }

    @Data
    private static class RunningBatchJobInfo {
        final Instant startTime = Instant.now();
        final String batchId;
        Long seconds;

        private RunningBatchJobInfo(String batchId) {
            this.batchId = batchId;
        }

        public long computeRunningSeconds() {
            seconds = Duration.between(startTime, Instant.now()).getSeconds();
            return seconds;
        }
    }
}

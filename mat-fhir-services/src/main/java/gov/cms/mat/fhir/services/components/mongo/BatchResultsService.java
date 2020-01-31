package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.services.summary.BatchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.OUTCOME_MISSING;

@Service
@Slf4j
public class BatchResultsService {

    private final ConversionResultsService conversionResultsService;

    public BatchResultsService(ConversionResultsService conversionResultsService) {
        this.conversionResultsService = conversionResultsService;
    }

    public Map<String, BatchResult> generateAggregationReport(String batchId) {

        List<ConversionResult> conversionResults = conversionResultsService.findByBatchId(batchId);

        Aggregator aggregator = new Aggregator(conversionResults);
        return aggregator.process();
    }

    static class Aggregator {
        final List<ConversionResult> conversionResults;

        Map<String, BatchResult> map = new HashMap<>();

        Aggregator(List<ConversionResult> conversionResults) {
            this.conversionResults = conversionResults;
        }

        Map<String, BatchResult> process() {

            conversionResults.forEach(this::processResult);

            map.values().forEach(BatchResult::compute);

            return map;
        }

        private void processResult(ConversionResult c) {

            if (c.getOutcome() == null) {
                c.setOutcome(OUTCOME_MISSING);
            }

            if (!map.containsKey(c.getOutcome().name())) {
                map.put(c.getOutcome().name(), new BatchResult());
            }

            BatchResult value = map.get(c.getOutcome().name());
            value.getIds().add(c.getMeasureId());

            if (c.getStart() != null && c.getFinished() != null) {
                long ns = Duration.between(c.getStart(), c.getFinished()).toMillis();
                value.getTimes().add(ns);
            } else {
                log.warn("Times are missing for ConversionResult: " + c.getMeasureId());
            }
        }
    }

}

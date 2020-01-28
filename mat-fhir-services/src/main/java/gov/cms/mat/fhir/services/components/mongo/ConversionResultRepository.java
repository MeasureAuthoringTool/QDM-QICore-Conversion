package gov.cms.mat.fhir.services.components.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ConversionResultRepository extends MongoRepository<ConversionResult, String> {

    Optional<ConversionResult> findByMeasureIdAndStart(String measureId, Instant start);

    List<ConversionResult> findByMeasureId(String measureId);

    Optional<ConversionResult> findTopByMeasureIdOrderByCreatedDesc(String measureId);

    Long countByBatchId(String batchId);

    List<ConversionResult> findByBatchId(String batchId);

    @Query(value = "{}", fields = "{'batchId' : 1}")
    List<ConversionResult> findAllBatchIds();
}

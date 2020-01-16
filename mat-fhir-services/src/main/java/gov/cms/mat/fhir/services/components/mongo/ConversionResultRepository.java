package gov.cms.mat.fhir.services.components.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;

public interface ConversionResultRepository extends MongoRepository<ConversionResult, String> {
    // List<ConversionResult> findByMeasureId(String measureId);

    Optional<ConversionResult> findByMeasureIdAndStart(String measureId, Instant start);
}

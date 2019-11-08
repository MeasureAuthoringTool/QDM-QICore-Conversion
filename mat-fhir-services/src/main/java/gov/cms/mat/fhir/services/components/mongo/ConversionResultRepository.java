package gov.cms.mat.fhir.services.components.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ConversionResultRepository extends MongoRepository<ConversionResult, String> {
    Optional<ConversionResult> findByMeasureId(String measureId);
}

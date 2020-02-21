package gov.cms.mat.fhir.services.components.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UnconvertedCqlLibraryRepository extends MongoRepository<UnconvertedCqlLibraryResult, String> {
    Optional<UnconvertedCqlLibraryResult> findByName(String name);

}

package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.FhirConversionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FhirConversionHistoryRepository extends JpaRepository<FhirConversionHistory, String> {
}

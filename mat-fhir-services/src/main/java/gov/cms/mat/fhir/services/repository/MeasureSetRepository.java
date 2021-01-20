package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.MeasureSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeasureSetRepository extends JpaRepository<MeasureSet, String> {
}

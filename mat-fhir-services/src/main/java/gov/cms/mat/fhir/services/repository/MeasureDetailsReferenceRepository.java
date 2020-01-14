package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.MeasureDetailsReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeasureDetailsReferenceRepository extends JpaRepository<MeasureDetailsReference, String> {
    @Query("select a from MeasureDetailsReference a where a.measureDetailsId = :measureDetailsId")
    List<MeasureDetailsReference> getMeasureDetailsReferenceByMeasureDetailsId(@Param("measureDetailsId") Integer measureDetailsId);
}

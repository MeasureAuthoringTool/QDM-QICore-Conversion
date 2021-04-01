package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.MeasureXml;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeasureXmlRepository extends JpaRepository<MeasureXml, String> {
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @Query("select a from MeasureXml a where a.measureId = :measureId")
    Optional<MeasureXml> findByMeasureId(@Param("measureId") String measureId);

    @Query(value = "select a.ID, a.SEVERE_ERROR_CQL, a.MEASURE_ID, a.MEASURE_XML from MEASURE_XML a where a.SEVERE_ERROR_CQL is not null", nativeQuery = true)
    List<MeasureXml> findErroredMeasureXml();
}

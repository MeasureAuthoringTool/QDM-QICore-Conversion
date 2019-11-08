package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.services.summary.MeasureVersionExportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MeasureExportRepository extends JpaRepository<MeasureExport, String> {
    @Query("select a from MeasureExport a where a.measureId = :measureId")
    MeasureExport getMeasureExportById(@Param("measureId") String measureId);

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @Query("select " +
            "new gov.cms.mat.fhir.services.summary.MeasureVersionExportId(me.measureExportId, ms.releaseVersion) " +
            "from MeasureExport me, Measure ms where me.measureId = ms.id and ms.releaseVersion in :allowedVersions")
    List<MeasureVersionExportId> getAllExportIdsAndVersion(Collection<String> allowedVersions);


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    Optional<MeasureExport> findById(String id);
}

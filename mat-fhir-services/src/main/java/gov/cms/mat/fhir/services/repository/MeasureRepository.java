package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.Measure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * /**
 *
 * @author duanedecouteau
 */
public interface MeasureRepository extends JpaRepository<Measure, String> {
    @Query("select a from Measure a where a.id = :id")
    Measure getMeasureById(@Param("id") String id);

    @Query("select a from Measure a where a.measureStatus = :measureStatus")
    List<Measure> getMeasuresByStatus(@Param("measureStatus") String measureStatus);

    @Query("select a.version from Measure a where a.id = :id")
    String findVersion(String id);

    Optional<Measure> findById(String id);

    @Query("select ms.id from Measure ms where ms.releaseVersion in :allowedVersions")
    List<String> findAllIdsWithAllowedVersions(Collection<String> allowedVersions);

    Optional<Measure> findByIdAndReleaseVersionIn(String measureId, List<String> allowedVersions);

    List<Measure> findByReleaseVersionIn(List<String> allowedVersions);
}

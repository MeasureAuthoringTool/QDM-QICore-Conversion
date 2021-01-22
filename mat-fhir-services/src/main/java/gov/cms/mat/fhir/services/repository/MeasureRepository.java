package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.Measure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MeasureRepository extends JpaRepository<Measure, String> {

    @Query("select ms.id from Measure ms")
    List<String> findAllIds();

    @Query("select ms.id from Measure ms where ms.draft = false and ms.releaseVersion in :allowedVersions")
    List<String> findAllIdsWithAllowedVersions(Collection<String> allowedVersions);

    @Query("select m from Measure m where m.measureModel = \'FHIR\' and m.measureSetId in " +
            "(select ms.measureSetId from Measure ms WHERE ms.measureSetId = m.measureSetId and " +
            "ms.measureModel = \'QDM\')")
    List<Measure> getAllConvertedFhirMeasures();
}

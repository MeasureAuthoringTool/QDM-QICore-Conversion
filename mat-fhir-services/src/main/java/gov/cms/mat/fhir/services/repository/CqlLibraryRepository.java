package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CqlLibraryRepository extends JpaRepository<CqlLibrary, String> {
    @Query("select a.id from CqlLibrary a where a.libraryModel = \'FHIR\' and a.measureId is null and a.draft = false order by a.lastModifiedOn asc")
    List<String> getAllVersionedCqlFhirLibs();

    @Query("select a from CqlLibrary a where a.libraryModel = \'FHIR\' and a.measureId is null order by a.lastModifiedOn asc")
    List<CqlLibrary> getAllStandaloneCqlFhirLibs();

    @Query("select a from CqlLibrary a where a.id = :id")
    CqlLibrary getCqlLibraryById(String id);

    Optional<CqlLibrary> findById(String id);

    @Query("select a from CqlLibrary a where a.measureId = :measureId")
    CqlLibrary getCqlLibraryByMeasureId(String measureId);

    @Query("select a from CqlLibrary a where a.cqlName = :cqlName and a.version = :version and a.libraryModel = 'FHIR' and a.draft = false")
    Optional<CqlLibrary> getVersionedCqlLibraryByNameAndVersion(String cqlName, BigDecimal version);

    List<CqlLibrary> findByQdmVersionAndCqlNameAndVersionAndLibraryModelAndFinalizedDateIsNotNull(String qdmVersion,
                                                                                                  String cqlName,
                                                                                                  BigDecimal version,
                                                                                                  String libraryModel);

    List<CqlLibrary> findByVersionAndCqlNameAndRevisionNumberAndLibraryModel(BigDecimal version,
                                                                             String cqlName,
                                                                             int revision,
                                                                             String libraryModel);

    CqlLibrary findByVersionAndCqlNameAndRevisionNumberAndLibraryModelAndDraft(BigDecimal version,
                                                                               String cqlName,
                                                                               int revision,
                                                                               String libraryModel,
                                                                               boolean draft);
}

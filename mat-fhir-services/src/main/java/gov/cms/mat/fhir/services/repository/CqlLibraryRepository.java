package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CqlLibraryRepository extends JpaRepository<CqlLibrary, String> {
    @Query("select a from CqlLibrary a where a.id = :id")
    CqlLibrary getCqlLibraryById(String id);

    Optional<CqlLibrary> findById(String id);

    @Query("select a from CqlLibrary a where a.measureId = :measureId")
    List<CqlLibrary> getCqlLibraryByMeasureId(String measureId);

    @Query("select a from CqlLibrary a where a.cqlName = :cqlName and a.version = :version")
    CqlLibrary getCqlLibraryByNameAndVersion(String cqlName, BigDecimal version);


    @Query("select a.releaseVersion from CqlLibrary a where a.id = :id")
    String findVersion(String id);

    List<CqlLibrary> findByQdmVersionAndCqlNameAndVersionAndLibraryModelAndFinalizedDateIsNotNull(String qdmVersion,
                                                                                                  String cqlName,
                                                                                                  BigDecimal version,
                                                                                                  String libraryModel);

    List<CqlLibrary> findByVersionAndCqlNameAndRevisionNumberAndLibraryModel(BigDecimal version,
                                                                             String cqlName,
                                                                             int revision,
                                                                            String libraryModel);

//    @Query("SELECT c.id " +
//            " FROM CqlLibrary c " +
//            "WHERE c.releaseVersion IN :allowedVersions")
//    List<String> findAllowedCqlLibraries(List<String> allowedVersions);

    @Query("FROM CqlLibrary c  WHERE c.releaseVersion IN :allowedVersions ")
    List<CqlLibrary> findAllowedCqlLibraries(List<String> allowedVersions);
}

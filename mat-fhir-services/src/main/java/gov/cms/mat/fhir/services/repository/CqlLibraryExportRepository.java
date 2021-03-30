package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CqlLibraryExportRepository extends JpaRepository<CqlLibraryExport, String> {
    @Query("select a from CqlLibraryExport a where a.cqlLibraryId = :cqlLibraryId")
    CqlLibraryExport getCqlLibraryExportByCqlLibraryId(@Param("cqlLibraryId") String cqlLibraryId);

    @Query(value = "select * from CQL_LIBRARY_EXPORT a where a.elm IS NOT NULL", nativeQuery = true)
    List<CqlLibraryExport> findExportedLibraries();
}

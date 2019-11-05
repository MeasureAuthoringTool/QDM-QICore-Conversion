/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * /**
 *
 * @author duanedecouteau
 */
public interface CqlLibraryExportRepository extends JpaRepository<CqlLibraryExport, String> {
    @Query("select a from CqlLibraryExport a where a.cqlLibraryId = :cqlLibraryId")
    CqlLibraryExport getCqlLibraryExportByCqlLibraryId(@Param("cqlLibraryId") String cqlLibraryId);
}

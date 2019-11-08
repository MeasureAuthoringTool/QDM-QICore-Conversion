/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * /**
 *
 * @author duanedecouteau
 */
public interface CqlLibraryRepository extends JpaRepository<CqlLibrary, String> {
    @Query("select a from CqlLibrary a where a.measureId = :measureId")
    List<CqlLibrary> getCqlLibraryByMeasureId(@Param("measureId") String measureId);

    @Query("select a.releaseVersion from CqlLibrary a where a.id = :id")
    String findVersion(String id);
}

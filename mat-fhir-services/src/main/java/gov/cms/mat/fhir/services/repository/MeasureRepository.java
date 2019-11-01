/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.Measure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}

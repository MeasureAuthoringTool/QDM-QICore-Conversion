/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.MeasureDetails;
import gov.cms.mat.fhir.commons.model.MeasureDetailsReference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author duanedecouteau
 */
public interface MeasureDetailsReferenceRepository extends JpaRepository<MeasureDetailsReference, String> {
    @Query("select a from MeasureDetailsReference a where a.measureDetailsId = :measureDetailsId")
    List<MeasureDetailsReference> getMeasureDetailsReferenceByMeasureDetailsId(@Param("measureDetailsId") Integer measureDetailsId);    
}

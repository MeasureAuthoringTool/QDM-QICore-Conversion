/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.repository;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
/**
 *
 * @author duanedecouteau
 */
public interface MeasureExportRepository extends JpaRepository<MeasureExport, String>{
    
    public List<MeasureExport> findAll();    

    @Query("select a from MeasureExport a where a.measureId = :measureId")
    public MeasureExport getMeasureExportById(@Param("measureId") String measureId);    

    
}

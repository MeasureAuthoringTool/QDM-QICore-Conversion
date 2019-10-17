/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.repository;
import gov.cms.mat.fhir.commons.model.Measure;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
/**
 *
 * @author duanedecouteau
 */
public interface MeasureRepository extends JpaRepository<Measure, String>{
    
    public List<Measure> findAll();

    @Query("select a from Measure a where a.id = :id")
    public Measure getMeasureById(@Param("id") String id);
    
}

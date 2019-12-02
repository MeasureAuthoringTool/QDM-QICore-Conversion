package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureXml;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MeasureXmlRepository extends JpaRepository<MeasureXml, String> {
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    Optional<MeasureXml> findByMeasureId(Measure measure);
}

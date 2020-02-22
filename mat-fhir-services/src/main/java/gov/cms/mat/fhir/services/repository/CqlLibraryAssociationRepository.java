package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.CqlLibraryAssociation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CqlLibraryAssociationRepository extends JpaRepository<CqlLibraryAssociation, String> {
    List<CqlLibraryAssociation> findByAssociationId(String associationId);
}

package gov.cms.mat.fhir.services.repository;

import gov.cms.mat.fhir.commons.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, String> {
}

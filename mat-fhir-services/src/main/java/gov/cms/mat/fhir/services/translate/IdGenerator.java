package gov.cms.mat.fhir.services.translate;

import java.util.UUID;

public interface IdGenerator {

    default String createId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
